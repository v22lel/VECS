package dev.v22.ecs;

import java.util.ArrayList;
import java.util.List;

public final class ComponentRegistry {
    private static final List<Class<? extends Component>> TYPES = new ArrayList<>();

    public static synchronized int register(Class<? extends Component> clazz) {
        int id = TYPES.size();
        TYPES.add(clazz);
        return id;
    }

    public static int getId(Class<? extends Component> clazz) {
        int idx = TYPES.indexOf(clazz);
        if (idx == -1) {
            return register(clazz);
        }
        return idx;
    }
}