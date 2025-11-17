package dev.v22.utils.create;

public interface Creatable {
    interface BaseCreator<T> {
        T[] createArray(int size);
    }
}
