package com.kevinguanchedarias.taggablecache.manager;

import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract class that provides default implementations
 *
 * @since 0.1.1
 */
public abstract class AbstractTaggableCacheManager implements TaggableCacheManager {
    @Override
    public void evictByCacheTag(String tag, Object part) {
        evictByCacheTag(tag + ":" + part);
    }

    /**
     * Returns the stored value, or if absent computes it , and then returns it
     *
     * @since 0.2.0
     */
    @Override
    public <T> T computeIfAbsent(String key, List<String> tags, Supplier<T> computeSupplier) {
        if (keyExists(key)) {
            return findByKey(key);
        } else {
            var computedValue = computeSupplier.get();
            saveEntry(key, computedValue, tags);
            return computedValue;
        }
    }
}