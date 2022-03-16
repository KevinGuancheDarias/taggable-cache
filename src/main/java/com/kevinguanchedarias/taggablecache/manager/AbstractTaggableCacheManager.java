package com.kevinguanchedarias.taggablecache.manager;

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
}
