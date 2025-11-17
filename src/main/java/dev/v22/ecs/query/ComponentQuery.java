package dev.v22.ecs.query;

import com.carrotsearch.hppc.BitSet;
import dev.v22.ecs.Archetype;

public interface ComponentQuery {
    BitSet getMask();

     static BitSet mask(int... ids) {
        BitSet res = new BitSet();
        for (int i : ids) {
            res.set(i);
        }
        return res;
    }

    void update(int i0, int i1, Archetype at);
}
