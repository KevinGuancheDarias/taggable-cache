package com.kevinguanchedarias.taggablecache.manager;

public abstract class AbstractTaggableCacheManager implements TaggableCacheManager {
    @Override
    public void evictByCacheTag(String tag, Object part) {
        evictByCacheTag(tag + ":" + part);
    }
}
