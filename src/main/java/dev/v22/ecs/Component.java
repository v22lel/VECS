package dev.v22.ecs;

import dev.v22.utils.create.Creatable;

public interface Component extends Creatable {
    void ensureEntities(int totalAmount);

    void swap(int a, int b);

    interface Creator extends BaseCreator<Component> {
        Component createNew(int baseAllocSize);

        @Override
        default Component[] createArray(int size) {
            return new Component[size];
        }
    }
}
