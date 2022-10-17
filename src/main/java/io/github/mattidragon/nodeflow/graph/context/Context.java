package io.github.mattidragon.nodeflow.graph.context;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Context {
    private final Map<ContextType<?>, Object> map;

    protected Context(Map<ContextType<?>, Object> map) {
        this.map = map;
    }

    public static Context empty() {
        return new Context(Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> T get(ContextType<T> type) {
        if (map.containsKey(type))
            // Only valid pairs can be inserted
            // noinspection unchecked
            return (T) map.get(type);

        for (var entry : map.entrySet()) {
            if (ArrayUtils.contains(entry.getKey().parents(), type))
                // Only valid pairs can be inserted
                // noinspection unchecked
                return (T) entry.getValue();
        }
        throw new NoSuchElementException("Missing graph context: " + type);
    }

    public boolean contains(ContextType<?> type) {
        if (map.containsKey(type)) return true;
        for (var entry : map.entrySet()) {
            if (ArrayUtils.contains(entry.getKey().parents(), type))
                return true;
        }
        return false;
    }

    public static class Builder {
        private final HashMap<ContextType<?>, Object> map = new HashMap<>();

        public <T> Builder put(ContextType<T> type, T value) {
            map.put(type, value);
            return this;
        }

        public Builder putAll(Context context) {
            map.putAll(context.map);
            return this;
        }

        public Context build() {
            return new Context(Map.copyOf(map));
        }
    }
}
