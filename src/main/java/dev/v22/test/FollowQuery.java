package dev.v22.test;

import dev.v22.Main;
import dev.v22.ecs.Archetype;
import dev.v22.ecs.query.RelationalQuery;

public class FollowQuery implements RelationalQuery {
    long result = 0;

    @Override
    public int getRelationType() {
        return Main.REL_FOLLOWING;
    }

    @Override
    public void update(Archetype a, Archetype b, int ia0, int ia1, int ib0, int ib1) {
        Pos p1 = a.getComponentById(Pos.ID);
        Pos p2 = b.getComponentById(Pos.ID);

        for (int ia = ia0; ia < ia1; ia++) {
            for (int ib = ib0; ib < ib1; ib++) {
                int xa = p1.x[ia];
                int ya = p1.y[ia];

                int xb = p2.y[ib];
                int yb = p2.y[ib];
                //do smth

                result += xa + xb;
                System.out.printf("follow executed (xa: %d, xb: %d)\n", xa, xb);
            }
        }
    }
}
