package dev.v22;

import dev.v22.ecs.Ecs;
import dev.v22.test.*;
import dev.v22.utils.create.CreatorException;

import java.util.BitSet;

public class Main {
    public static int REL_FOLLOWING;

    static void main() {
        try {
            Ecs ecs = new Ecs(1024, 5);

            REL_FOLLOWING = ecs.newRelationType();

            ecs.registerQuery(new FollowQuery());

            int ENTITY_COUNT = 3;

            System.out.println("Spawning " + ENTITY_COUNT + " entities…");

            int center = ecs.spawnEntities(1, null, Pos.class)[0];

            int[] entities1 = ecs.spawnEntities(ENTITY_COUNT, (i, comps) -> {
                Pos pos = (Pos) comps[0];
                pos.x[i] = i;
            }, Pos.class, Vel.class, Accel.class, Health.class);

            int[] entities2 = ecs.spawnEntities(ENTITY_COUNT, (i, comps) -> {
                Pos pos = (Pos) comps[0];
                pos.x[i] = i;
            }, Pos.class);

            int follow1 = ecs.newRelations(entities1, REL_FOLLOWING, center);
            int follow2 = ecs.newRelations(entities2, REL_FOLLOWING, center);

            ecs.destroyRelations(follow1);

            ecs.addRelationsEntities(follow2, entities1);

            System.out.println("warming jvm and cache queries");

            for (int i = 0; i < 2; i++) {
                ecs.update();
            }

            System.out.println("starting benchmark");

            long now = System.currentTimeMillis();

            for (int i = 0; i < 1; i++) {
                ecs.update();
            }

            long elapsed = System.currentTimeMillis() - now;
            long frame = elapsed / 60;
            System.out.println("total time: " + elapsed + "ms");
            System.out.println("frame time: " + frame + "ms");

            ecs.shutdown();

        } catch (CreatorException e) {
            throw new RuntimeException(e);
        }
    }
}
