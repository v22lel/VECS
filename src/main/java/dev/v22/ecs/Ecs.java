package dev.v22.ecs;

import com.carrotsearch.hppc.BitSet;
import dev.v22.ecs.parallel.EcsWorker;
import dev.v22.ecs.query.ComponentQuery;
import dev.v22.ecs.query.RelationalQuery;
import dev.v22.ecs.relations.Relations;
import dev.v22.ecs.utils.IntArray;
import dev.v22.utils.Utils;
import dev.v22.utils.create.CreatorException;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Ecs {
    private final int baseEntityAlloc, numWorkerThreads;

    private CyclicBarrier updateBarrier;
    private EcsWorker[] workers;

    private Relations relations;

    private final List<Archetype> archetypes = new ArrayList<>();
    private final List<ComponentQueryEntry> componentQueries = new ArrayList<>();
    private final List<RelationalQueryEntry> relationalQueries = new ArrayList<>();
    private final List<EntityLocation> locations = new ArrayList<>();

    // --- New fields to support stable ordering of archetypes ---
    // assign a small integer id to each archetype when it is first observed
    private final Map<Archetype, Integer> archetypeIds = new IdentityHashMap<>();
    private int nextArchetypeId = 0;

    private int relTypeIdCounter;

    public Ecs(int baseEntityAlloc, int numWorkerThreads) {
        this.baseEntityAlloc = baseEntityAlloc;
        this.numWorkerThreads = numWorkerThreads;

        updateBarrier = new CyclicBarrier(numWorkerThreads);
        workers = new EcsWorker[numWorkerThreads];
        for (int i = 0; i < numWorkerThreads; i++) {
            workers[i] = new EcsWorker();
            workers[i].start();
        }

        relations = new Relations(baseEntityAlloc / 10); //idk feels good
    }

    public int getBaseEntityAllocation() {
        return baseEntityAlloc;
    }

    public void registerQuery(ComponentQuery q) {
        ComponentQueryEntry entry = new ComponentQueryEntry(q);
        componentQueries.add(entry);

        for (Archetype at : archetypes) {
            if (Utils.containsAllBits(at.getMask(), q.getMask())) {
                entry.matches.add(at);
            }
        }
    }

    public void registerQuery(RelationalQuery q) {
        RelationalQueryEntry entry = new RelationalQueryEntry(q);
        relationalQueries.add(entry);
        clearRelationalCaches();
    }

    public synchronized int newRelationType() {
        return relTypeIdCounter++;
    }

    @SafeVarargs
    public final int[] spawnEntities(int amount, EntityInitializer initializer, Class<? extends Component>... needed) throws CreatorException {
        int[] ids = new int[needed.length];
        int i = 0;
        for (Class<? extends Component> c : needed) {
            int id = ComponentRegistry.getId(c);
            ids[i++] = id;
        }

        BitSet mask = ComponentQuery.mask(ids);

        Archetype found = null;
        for (Archetype a : archetypes)
            if (a.getMask().equals(mask))
                found = a;

        if (found == null) {
            found = new Archetype(this, mask, ids, needed);
            archetypes.add(found);
            // assign stable id for ordering
            archetypeIds.put(found, nextArchetypeId++);

            for (ComponentQueryEntry e : componentQueries) {
                if (Utils.containsAllBits(mask, e.mask)) {
                    e.matches.add(found);
                }
            }
        }

        int[] archetypeIndices = found.createEntities(amount, initializer, locations.size());

        int[] res = new int[amount];
        int i2 = 0;
        for (int idx : archetypeIndices) {
            EntityLocation location = new EntityLocation(found, idx);
            res[i2++] = locations.size();
            locations.add(location);
        }

        return res;
    }

    public final void destroyEntity(int entityId) {
        clearRelationalCaches();
        EntityLocation loc = locations.get(entityId);
        Archetype at = loc.getArchetype();
        int idx = loc.getIndex();

        int movedId = at.destroyEntity(idx);

        locations.set(entityId, null);

        if (movedId != entityId) {
            EntityLocation movedLoc = locations.get(movedId);
            if (movedLoc != null) {
                movedLoc.setIndex(idx);
            }
        }
    }

    public int newRelations(int[] sourceEntities, int relationType, int targetEntity) {
        clearRelationalCaches();
        return relations.newRelations(sourceEntities, relationType, targetEntity);
    }

    public void modifyRelations(int relationsId, int newRelationType, int newTargetEntity) {
        clearRelationalCaches();
        relations.modifyRelations(relationsId, newRelationType, newTargetEntity);
    }

    public void removeRelationsEntities(int relationsId, int[] sourceEntities) {
        clearRelationalCaches();
        relations.removeRelations(relationsId, sourceEntities);
    }

    public void addRelationsEntities(int relationsId, int[] sourceEntities) {
        clearRelationalCaches();
        relations.addRelations(relationsId, sourceEntities);
    }

    public void destroyRelations(int relationsId) {
        clearRelationalCaches();
        relations.destroyRelations(relationsId);
    }

    public void update() {
        for (ComponentQueryEntry q : componentQueries) {
            for (Archetype at : q.matches) {
                int totalEntities = at.getEntityCount();
                int chunkSize = Math.max(totalEntities / numWorkerThreads, 1);

                int workerCount = Math.min(numWorkerThreads, totalEntities);
                CountDownLatch latch = new CountDownLatch(workerCount);

                int i0 = 0;
                for (int w = 0; w < workerCount; w++) {
                    int i1 = Math.min(i0 + chunkSize, totalEntities);
                    final int from = i0;
                    final int to = i1;

                    Runnable task = () -> {
                        q.componentQuery.update(from, to, at);
                        latch.countDown();
                    };

                    workers[w].submitTask(task);
                    i0 = i1;
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }
        runRelationalQueries();
    }

    private void runRelationalQueries() {
        for (RelationalQueryEntry rqe : relationalQueries) {
            if (rqe.cacheEntries.isEmpty()) {
                buildRelationalQueryCache(rqe);
            }

            for (RelationalQueryEntry.CacheEntry cacheEntry : rqe.cacheEntries) {
                rqe.query.update(
                        cacheEntry.sourcesArch,
                        cacheEntry.targetArch,
                        cacheEntry.sourcesStart,
                        cacheEntry.sourcesEnd,
                        cacheEntry.targetIdx,
                        cacheEntry.targetIdx + 1
                );
            }
        }
    }

    private void clearRelationalCaches() {
        for (RelationalQueryEntry rqe : relationalQueries) {
            rqe.cacheEntries.clear();
        }
    }

    private void buildRelationalQueryCache(RelationalQueryEntry rqe) {
        List<RelationalQueryEntry.CacheEntry> cacheEntries = rqe.cacheEntries;
        cacheEntries.clear();

        RelationalQuery q = rqe.query;
        int type = q.getRelationType();
        if (!relations.supportsType(type)) return;

        IntArray relIds = relations.getRelationsForType(type);

        // Temporary per-build map: one range builder per source archetype.
        // IdentityHashMap because different Archetype instances are distinct.
        Map<Archetype, RangeBuilder> ranges = new IdentityHashMap<>();

        for (int i = 0; i < relIds.elementsCount; i++) {
            int relId = relIds.buffer[i];

            IntArray sourcesArr = relations.getSources(relId);
            if (sourcesArr == null) continue;

            int targetEntity = relations.getTarget(relId);
            EntityLocation tgtLoc = locations.get(targetEntity);
            if (tgtLoc == null) continue;

            Archetype tgtArch = tgtLoc.getArchetype();
            int tgtIndex = tgtLoc.getIndex();

            ranges.clear();

            for (int s = 0; s < sourcesArr.elementsCount; s++) {
                int srcEntity = sourcesArr.buffer[s];
                EntityLocation srcLoc = locations.get(srcEntity);
                if (srcLoc == null) continue;

                Archetype srcArch = srcLoc.getArchetype();
                int srcIndex = srcLoc.getIndex();

                RangeBuilder rb = ranges.get(srcArch);
                if (rb == null) {
                    rb = new RangeBuilder();
                    ranges.put(srcArch, rb);
                }
                rb.addIndex(srcIndex);
            }

            // Emit final ranges
            for (Map.Entry<Archetype, RangeBuilder> e : ranges.entrySet()) {
                Archetype srcArch = e.getKey();
                RangeBuilder rb = e.getValue();

                for (int r = 0; r < rb.count; r++) {
                    int start = rb.starts[r];
                    int end   = rb.ends[r];

                    cacheEntries.add(
                            new RelationalQueryEntry.CacheEntry(
                                    srcArch,
                                    tgtArch,
                                    tgtIndex,
                                    start,
                                    end
                            )
                    );
                }
            }
        }
    }

    public void shutdown() {
        for (EcsWorker w : workers) w.shutdown();
    }

    private static class ComponentQueryEntry {
        final ComponentQuery componentQuery;
        final BitSet mask;
        final List<Archetype> matches = new ArrayList<>();

        ComponentQueryEntry(ComponentQuery q) {
            this.componentQuery = q;
            this.mask = q.getMask();
        }
    }

    private static class RelationalQueryEntry {
        final RelationalQuery query;
        final List<CacheEntry> cacheEntries;

        RelationalQueryEntry(RelationalQuery q) {
            this.query = q;
            cacheEntries = new ArrayList<>();
        }

        private static class CacheEntry {
            final Archetype sourcesArch;
            final Archetype targetArch;
            final int targetIdx;
            final int sourcesStart;
            final int sourcesEnd;

            public CacheEntry(Archetype sourcesArch, Archetype targetArch, int targetIdx, int sourcesStart, int sourcesEnd) {
                this.sourcesArch = sourcesArch;
                this.targetArch = targetArch;
                this.targetIdx = targetIdx;
                this.sourcesStart = sourcesStart;
                this.sourcesEnd = sourcesEnd;
            }
        }
    }

    private static class RangeBuilder {
        int[] starts = new int[4];
        int[] ends   = new int[4];
        int count = 0;

        private int lastIndex = -1;
        private boolean hasCurrent = false;

        void addIndex(int idx) {
            if (!hasCurrent) {
                startNew(idx);
                return;
            }
            if (idx == lastIndex + 1) {
                ends[count - 1] = idx + 1;
                lastIndex = idx;
            } else {
                startNew(idx);
            }
        }

        private void startNew(int idx) {
            ensureCapacity();
            starts[count] = idx;
            ends[count] = idx + 1;
            lastIndex = idx;
            hasCurrent = true;
            count++;
        }

        private void ensureCapacity() {
            if (count >= starts.length) {
                starts = Arrays.copyOf(starts, starts.length * 2);
                ends   = Arrays.copyOf(ends,   ends.length * 2);
            }
        }
    }

}
