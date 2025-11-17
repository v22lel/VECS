package dev.v22.test;

import com.carrotsearch.hppc.BitSet;
import dev.v22.ecs.Archetype;
import dev.v22.ecs.query.ComponentQuery;

public class ComplexComponentQuery implements ComponentQuery {

    @Override
    public BitSet getMask() {
        return ComponentQuery.mask(Pos.ID, Vel.ID, Accel.ID, Health.ID);
    }

    @Override
    public void update(int i0, int i1, Archetype at) {
        Pos pos       = at.getComponentById(Pos.ID);
        Vel vel       = at.getComponentById(Vel.ID);
        Accel acc     = at.getComponentById(Accel.ID);
        Health health = at.getComponentById(Health.ID);

        int[] px = pos.x;
        int[] py = pos.y;

        float[] vx = vel.x;
        float[] vy = vel.y;

        float[] ax = acc.x;
        float[] ay = acc.y;

        int[] hp = health.hp;

        for (int i = i0; i < i1; i++) {

            // physics integration
            vx[i] += ax[i] * 0.016f;
            vy[i] += ay[i] * 0.016f;

            px[i] += (int) vx[i];
            py[i] += (int) vy[i];

            // simple damage logic
            if ((vx[i] * vx[i] + vy[i] * vy[i]) > 50f)
                hp[i] -= 1;
        }
    }
}