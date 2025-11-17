package dev.v22.ecs;

public class EntityLocation {
    private Archetype archetype;
    private int index;

    public EntityLocation(Archetype archetype, int index) {
        this.archetype = archetype;
        this.index = index;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
