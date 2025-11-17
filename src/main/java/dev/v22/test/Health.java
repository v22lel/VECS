package dev.v22.test;

import dev.v22.ecs.Component;
import dev.v22.ecs.ComponentRegistry;
import dev.v22.utils.Utils;

import java.util.Arrays;

public class Health implements Component {
    public static final int ID = ComponentRegistry.register(Health.class);

    public int[] hp;

    @Override
    public void ensureEntities(int totalAmount) {
        hp = Arrays.copyOf(hp, totalAmount);
    }

    @Override
    public void swap(int a, int b) {
        Utils.arraySwap(hp, a, b);
    }

    public static final Creator CREATOR = baseAllocSize -> {
        Health h = new Health();
        h.hp = new int[baseAllocSize];
        return h;
    };
}
