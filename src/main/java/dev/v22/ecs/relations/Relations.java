package dev.v22.ecs.relations;

import dev.v22.ecs.EntityLocation;
import dev.v22.ecs.query.RelationalQuery;
import dev.v22.ecs.utils.IntArray;
import dev.v22.utils.Utils;

import java.util.Arrays;

public class Relations {
    // Explanation: The top level array index is the relId, so this class is a SoA
    private IntArray[] sources;
    private int[] relType;
    private int[] target;

    private IntArray[] byType;

    private int idCounter;

    //instead of swap-removing relations, keep the "holes" and fill them up later. this way
    //outside relIds are never invalidated
    private final IntArray nextFreeIds;

    public Relations(int baseRelAlloc) {
        sources = new IntArray[baseRelAlloc];
        relType = new int[baseRelAlloc];
        target = new int[baseRelAlloc];
        nextFreeIds = new IntArray();
        byType = new IntArray[0];
    }

    public int newRelations(int[] sources, int type, int target) {
        int relId;
        if (nextFreeIds.isEmpty()) {
            relId = idCounter++;
            growArrays(1);
        } else {
            //remove last is cheap for IntArray
            relId = nextFreeIds.removeLast();
        }
        IntArray sourcesArr = new IntArray(sources.length);
        System.arraycopy(sources, 0, sourcesArr.buffer, 0, sources.length);
        sourcesArr.elementsCount = sources.length;

        this.sources[relId] = sourcesArr;
        this.relType[relId] = type;
        this.target[relId] = target;

        if (type >= byType.length) {
            int oldEnd = byType.length;
            byType = Arrays.copyOf(byType, type + 1);
            for (int i = oldEnd; i < byType.length; i++) {
                byType[i] = new IntArray(4); //idk feel like baseAlloc of 4 is nice
            }
        }

        IntArray typeArr = byType[type];
        typeArr.add(relId);

        return relId;
    }

    public void destroyRelations(int relId) {
        if (relId < 0 || relId >= sources.length) return;
        nextFreeIds.add(relId);
        sources[relId] = null;
        int oldTarget = target[relId];

        int oldType = relType[relId];
        IntArray oldTypeArr = byType[oldType];
        oldTypeArr.removeElement(relId);
    }

    public void modifyRelations(int relId, int newType, int newTarget) {
        if (relId < 0 || relId >= sources.length) return;
        int oldType = relType[relId];
        if (newType >= 0) {
            relType[relId] = newType;
        }
        if (newTarget >= 0) {
            target[relId] = newTarget;
        }

        if (newType >= 0 && oldType != newType) {
            if (newType >= byType.length) {
                int oldEnd = byType.length;
                byType = Arrays.copyOf(byType, newType + 1);
                for (int i = oldEnd; i < byType.length; i++) {
                    byType[i] = new IntArray(4); //idk feel like baseAlloc of 4 is nice
                }
            }

            IntArray typeArr = byType[newType];
            typeArr.add(relId);

            //remove old occurence
            IntArray oldTypeArr = byType[oldType];
            oldTypeArr.removeElement(relId);
        }
    }

    public void addRelations(int relId, int[] sources) {
        if (relId < 0 || relId >= this.sources.length) return;
        IntArray arr = this.sources[relId];
        arr.ensureCapacity(arr.elementsCount + sources.length);
        System.arraycopy(sources, 0, arr.buffer, arr.elementsCount, sources.length);
        arr.elementsCount += sources.length;
    }

    public void removeRelations(int relId, int[] sources) {
        if (relId < 0 || relId >= this.sources.length) return;
        IntArray arr = this.sources[relId];
        arr.removeAll(x -> Utils.arrayContains(sources, x));
    }

    public void entityDestroyed(int entity) {
        //TODO: clear all relations using this entity either as source or target
    }

    private void growArrays(int sizeMod) {
        int newLen = sources.length + sizeMod;
        sources = Arrays.copyOf(sources, newLen);
        relType = Arrays.copyOf(relType, newLen);
        target = Arrays.copyOf(target, newLen);
    }

    public IntArray getRelationsForType(int relType) {
        return byType[relType];
    }

    public boolean supportsType(int relType) {
        return relType >= 0 && relType < byType.length;
    }

    public IntArray getSources(int relId) {
        return sources[relId];
    }

    public int getTarget(int relId) {
        return target[relId];
    }
}
