package dev.v22.test;

import com.carrotsearch.hppc.BitSet;
import dev.v22.ecs.Archetype;
import dev.v22.ecs.query.ComponentQuery;

public class MoveComponentQuery implements ComponentQuery {
    @Override
    public BitSet getMask() {
        return ComponentQuery.mask(Pos.ID, Vel.ID);
    }

    @Override
    public void update(int i0, int i1, Archetype at) {
        Pos pos = at.getComponentById(Pos.ID);
        Vel vel = at.getComponentById(Vel.ID);

        for (int i = i0; i < i1; i++) {
            pos.x[i] += (int) vel.x[i];
            pos.y[i] += (int) vel.y[i];
        }
    }
}
