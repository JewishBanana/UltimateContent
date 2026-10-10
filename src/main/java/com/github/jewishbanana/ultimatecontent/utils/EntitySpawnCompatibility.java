package com.github.jewishbanana.ultimatecontent.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

/** Adapts pre-java.util.function.Consumer Bukkit spawn overloads without NMS. */
public final class EntitySpawnCompatibility {
    private static final Method LEGACY_SPAWN = findLegacySpawn();

    private EntitySpawnCompatibility() {}

    private static Method findLegacySpawn() {
        try {
            World.class.getMethod("spawn", Location.class, Class.class, boolean.class, Consumer.class);
            return null;
        } catch (NoSuchMethodException oldApi) {
            try {
                return World.class.getMethod("spawn", Location.class, Class.class, boolean.class,
                        Class.forName("org.bukkit.util.Consumer"));
            } catch (ReflectiveOperationException unsupported) {
                throw new IllegalStateException("No supported Bukkit entity spawn method", unsupported);
            }
        }
    }

    public static <T extends Entity> T spawn(Location location, Class<T> type, Consumer<? super T> initializer) {
        return spawn(location, type, true, initializer);
    }

    public static <T extends Entity> T spawn(Location location, Class<T> type, boolean randomizeData,
            Consumer<? super T> initializer) {
        if (LEGACY_SPAWN == null)
            return location.getWorld().spawn(location, type, randomizeData, initializer);
        // The initializer must still execute BEFORE the entity enters the world:
        // applying it after spawn would expose unconfigured model stands to listeners.
        Class<?> legacyConsumer = LEGACY_SPAWN.getParameterTypes()[3];
        Object callback = initializer == null ? null : Proxy.newProxyInstance(legacyConsumer.getClassLoader(),
                new Class<?>[] {legacyConsumer}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "accept": initializer.accept(type.cast(args[0])); return null;
                        case "toString": return "UltimateContent spawn initializer";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        default: throw new UnsupportedOperationException(method.toString());
                    }
                });
        try {
            return type.cast(LEGACY_SPAWN.invoke(location.getWorld(), location, type, randomizeData, callback));
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Bukkit could not spawn " + type.getName(), cause);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot access Bukkit's entity spawn method", failure);
        }
    }
}
