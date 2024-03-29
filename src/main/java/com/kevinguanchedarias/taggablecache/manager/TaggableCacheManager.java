package com.kevinguanchedarias.taggablecache.manager;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Interface that provides the contract that Taggable Cache managers must follow
 *
 * @since 0.1.0
 */
public interface TaggableCacheManager {

    /**
     * Check if the specified key exists
     *
     * @since 0.1.0
     */
    boolean keyExists(String key);

    /**
     * @return specified key or null if none
     * @since 0.1.0
     */
    <T> T findByKey(String key);

    /**
     * Evict all keys that have the specified tag
     *
     * @since 0.1.0
     */
    void evictByCacheTag(String tag);

    /**
     * Evict all keys that have the specified tag with part
     *
     * @since 0.1.0
     */
    void evictByCacheTag(String tag, Object part);

    /**
     * Saves an entry to the cache <br>
     * Implementations may throw {@link IllegalStateException} if tried to insert an already inserted value
     *
     * @param key   The key
     * @param value The value of the entry
     * @param tags  Tags that will remove this cache entry if evicted
     * @since 0.1.0
     */
    void saveEntry(String key, Object value, Collection<String> tags);

    /**
     * Evict cache entry by key
     *
     * @since 0.1.0
     */
    void evictByKey(String key);

    <T> T computeIfAbsent(String key, List<String> tags, Supplier<T> computeSupplier);

    /**
     * Deletes all the cache
     *
     * @since 0.1.4
     */
    void clear();
}
