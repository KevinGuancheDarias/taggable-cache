package com.kevinguanchedarias.taggablecache.manager;

import java.util.Collection;

public interface TaggableCacheManager {

    boolean keyExists(String key);

    Object findByKey(String key);

    void evictByCacheTag(String tag);

    void saveEntry(String key, Object value, Collection<String> tags);

    void evictByKey(String key);
}
