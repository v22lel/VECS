package dev.v22.ecs.parallel;

import dev.v22.ecs.Archetype;
import dev.v22.ecs.query.ComponentQuery;

import java.util.concurrent.CyclicBarrier;

public class UpdateTask {
    private ComponentQuery componentQuery;
    private Archetype archetype;
    private int i0, i1;
    private CyclicBarrier barrier;

    public UpdateTask(ComponentQuery componentQuery, Archetype archetype, int i0, int i1, CyclicBarrier barrier) {
        this.componentQuery = componentQuery;
        this.archetype = archetype;
        this.i0 = i0;
        this.i1 = i1;
        this.barrier = barrier;
    }

    public void process() throws Exception {
        componentQuery.update(i0, i1, archetype);
        barrier.await();
    }
}
