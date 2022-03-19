package com.kevinguanchedarias.taggablecache.manager;

import com.kevinguanchedarias.taggablecache.configuration.properties.ConcurrentHashMapTaggableProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation for {@link TaggableCacheManager} using in memory hash maps
 *
 * @since 0.1.0
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConcurrentHashMapTaggableCacheManager extends AbstractTaggableCacheManager {
    private final Map<String, Object> dataStore;
    private final Map<String, LocalDateTime> dataStoreTtl;
    private final Map<String, Set<String>> tagsToCacheKeys;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Lock lock;
    private final ConcurrentHashMapTaggableProperties concurrentHashMapTaggableProperties;

    private interface NullValue {
    }

    public ConcurrentHashMapTaggableCacheManager(ConcurrentHashMapTaggableProperties concurrentHashMapTaggableProperties) {
        dataStore = new ConcurrentHashMap<>();
        dataStoreTtl = new HashMap<>();
        tagsToCacheKeys = new ConcurrentHashMap<>();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        lock = new ReentrantLock();
        this.concurrentHashMapTaggableProperties = concurrentHashMapTaggableProperties;
    }

    @PostConstruct
    public void autoKeyWipe() {
        scheduledExecutorService.scheduleAtFixedRate(
                this::doWipeKeys,
                concurrentHashMapTaggableProperties.getCacheTtl(),
                concurrentHashMapTaggableProperties.getCacheTtl(),
                concurrentHashMapTaggableProperties.getTimeUnit()
        );
    }

    @Override
    public boolean keyExists(String key) {
        return dataStore.containsKey(key);
    }

    @Override
    public Object findByKey(String key) {
        var storedValue = dataStore.get(key);
        return storedValue instanceof NullValue ? null : storedValue;
    }

    @Override
    public void evictByCacheTag(String tag) {
        lock.lock();
        try {
            if (tagsToCacheKeys.containsKey(tag)) {
                tagsToCacheKeys.get(tag).forEach(this::deleteKey);
                tagsToCacheKeys.get(tag).clear();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void saveEntry(String key, Object value, Collection<String> tags) {
        lock.lock();
        try {
            if (dataStore.containsKey(key)) {
                throw new IllegalStateException("Tried to update a key that is already stored");
            }
            dataStore.put(key, value == null ?
                    new NullValue() {
                    }
                    : value);
            dataStoreTtl.put(key, LocalDateTime.now().plus(
                    concurrentHashMapTaggableProperties.getCacheTtl(), concurrentHashMapTaggableProperties.getTimeUnit().toChronoUnit()
            ));
            tags.forEach(tag -> addKeyToTagStore(key, tag));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void evictByKey(String key) {
        lock.lock();
        deleteKey(key);
        lock.unlock();
        scheduledExecutorService.execute(() -> {
            lock.lock();
            try {
                wipeCacheKeyFromTagsStore(key);
            } finally {
                lock.unlock();
            }
        });
    }

    private void addKeyToTagStore(String cacheKey, String tag) {
        if (tagsToCacheKeys.containsKey(tag)) {
            tagsToCacheKeys.get(tag).add(cacheKey);
        } else {
            Set<String> list = new HashSet<>();
            list.add(cacheKey);
            tagsToCacheKeys.put(tag, list);
        }
    }

    private void doWipeKeys() {
        lock.lock();
        try {
            dataStoreTtl.entrySet().stream()
                    .filter(entry -> entry.getValue().isBefore(LocalDateTime.now()))
                    .map(Map.Entry::getKey)
                    .toList()
                    .forEach(key -> {
                        deleteKey(key);
                        wipeCacheKeyFromTagsStore(key);
                    });
        } finally {
            lock.unlock();
        }
    }

    private void wipeCacheKeyFromTagsStore(String key) {
        tagsToCacheKeys.values().stream()
                .filter(storedKeys -> storedKeys.contains(key))
                .forEach(storedKeys -> storedKeys.remove(key));
    }

    private void deleteKey(String key) {
        dataStore.remove(key);
        dataStoreTtl.remove(key);
    }
}
