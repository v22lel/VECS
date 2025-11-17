package dev.v22.utils.create;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Creators {
    private static final HashMap<Class<? extends Creatable>, ? extends Creatable.BaseCreator<?>> CACHE = new HashMap<>();

    public static <T extends Creatable, R extends Creatable.BaseCreator<T>> R getCreator(Class<? extends Creatable> clazz) throws CreatorException {
        if (CACHE.containsKey(clazz)) {
            return (R) CACHE.get(clazz);
        }

        try {
            Field field = clazz.getDeclaredField("CREATOR");
            field.setAccessible(true);
            return (R) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new CreatorException(CreatorException.Cause.NO_CREATOR_FOUND);
        } catch (IllegalAccessException e) {
            throw new CreatorException(CreatorException.Cause.INVALID_CREATOR);
        }
    }
}
