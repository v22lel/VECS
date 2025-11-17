package dev.v22.ecs;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntHashMap;
import dev.v22.utils.create.CreatorException;
import dev.v22.utils.create.Creators;

public class Archetype {
    private final BitSet mask;
    private final Component[] components;
    private int entityCount = 0;
    private IntArrayList entities;
    private IntIntHashMap globalLocalMap;
    private final Ecs ecs;

    public Archetype(Ecs ecs, BitSet mask, int[] ids, Class<? extends Component>[] classes) throws CreatorException {
        this.ecs = ecs;
        this.mask = mask;
        this.entities = new IntArrayList();
        this.globalLocalMap = new IntIntHashMap();

        components = new Component[classes.length];
        for (int i = 0; i < classes.length; i++) {
            Component.Creator c = Creators.getCreator(classes[i]);
            components[i] = c.createNew(ecs.getBaseEntityAllocation());
            globalLocalMap.put(ids[i], i);
        }
    }

    public BitSet getMask() { return mask; }

    public int[] createEntities(int amount, EntityInitializer initializer, int baseEntityId) {
        int old = entityCount;
        entityCount += amount;

        for (Component c : components)
            c.ensureEntities(entityCount);

        int[] results = new int[amount];
        for (int i = 0; i < amount; i++) {
            results[i] = old + i;
            if (initializer != null) {
                initializer.init(old + i, components);
            }
            entities.add(baseEntityId + i);
        }
        return results;
    }

    public int destroyEntity(int idx) {
        int last = entityCount - 1;

        int movedEntityId = entities.get(last);

        for (Component c : components) {
            c.swap(idx, last);
        }

        entities.set(idx, movedEntityId);
        entities.removeAt(last);
        entityCount--;

        return movedEntityId;
    }

    public int getEntityCount() { return entityCount; }

    public Component[] getComponents() { return components; }

    public int getEntityIdAtIndex(int idx) {
        return entities.get(idx);
    }

    public <T extends Component> T getComponentById(int id) {
        int localIdx = globalLocalMap.get(id);
        return (T) components[localIdx];
    }
}