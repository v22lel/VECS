package dev.v22.test;

import dev.v22.ecs.Component;
import dev.v22.ecs.ComponentRegistry;
import dev.v22.utils.Utils;

import java.util.Arrays;

public class Vel implements Component {
    public static final int ID = ComponentRegistry.register(Vel.class);

    public float[] x, y;

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
        Vel c = new Vel();
        c.x = new float[baseAllocSize];
        c.y = new float[baseAllocSize];
        return c;
    };
}
