package dev.v22.test;

import dev.v22.ecs.Component;
import dev.v22.ecs.ComponentRegistry;
import dev.v22.utils.Utils;

import java.util.Arrays;

public class Pos implements Component {
    public static final int ID = ComponentRegistry.register(Pos.class);

    public int[] x, y;

    @Override
    public void ensureEntities(int totalAmount) {
        x = Arrays.copyOf(x, totalAmount);
        y = Arrays.copyOf(y, totalAmount);
    }

    @Override
    public void swap(int a, int b) {
        Utils.arraySwap(x, a, b);
        Utils.arraySwap(y, a, b);
    }

    public static final Creator CREATOR = baseAllocSize -> {
        Pos c = new Pos();
        c.x = new int[baseAllocSize];
        c.y = new int[baseAllocSize];
        return c;
    };
}
