package dev.v22.ecs.utils;

import com.carrotsearch.hppc.ArraySizingStrategy;
import com.carrotsearch.hppc.BufferAllocationException;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.comparators.IntComparator;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.sorting.QuickSort;
import dev.v22.utils.Utils;

import java.util.Comparator;

public class IntArray extends IntArrayList {
    public IntArray() {
        super(0, new PHIGrowth());
    }

    public IntArray(int expectedElements) {
        super(expectedElements, new PHIGrowth());
    }

    public IntArray(IntContainer container) {
        super(container.size(), new PHIGrowth());
        for (IntCursor c : container) {
            add(c.value);
        }
    }

    public int swapRemove(int i) {
        if (isEmpty()) return 0;
        int lastIdx = size() - 1;
        Utils.arraySwap(buffer, i, lastIdx);
        return removeLast();
    }

    public void sortBy(IntComparator cmp) {
        QuickSort.sort(buffer, 0, size() - 1, cmp::compare);
    }

    private static class PHIGrowth implements ArraySizingStrategy {
        private static final float PHI = 1.61803398874989490252573887119069695472717285156250f;
        private long allocated, used;

        @Override
        public int grow(int capacity, int size, int additions) throws BufferAllocationException {
            int newSize = capacity > 0 ? capacity : 1;
            while (size + additions > newSize) {
                newSize = (int) (((float) newSize) * PHI);
            }
            allocated = ((long) newSize) * Integer.BYTES;
            used = ((long) size + additions) * Integer.BYTES;
            return newSize;
        }

        @Override
        public long ramBytesAllocated() {
            return allocated;
        }

        @Override
        public long ramBytesUsed() {
            return used;
        }
    }
}
