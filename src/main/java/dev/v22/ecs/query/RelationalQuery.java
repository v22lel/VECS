package dev.v22.ecs.query;

import dev.v22.ecs.Archetype;

public interface RelationalQuery {
    int getRelationType();

    void update(Archetype a, Archetype b, int ia0, int ia1, int ib0, int ib1);
}
