package com.kevinguanchedarias.taggablecache.manager;

import com.kevinguanchedarias.taggablecache.configuration.properties.ConcurrentHashMapTaggableProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ConcurrentHashMapTaggableCacheManagerTest {
    private static final String TEST_KEY = "foo_key";
    private static final String TEST_VALUE = "the_val";
    private static final String TEST_TAG = "the-tag";
    private static final String TEST_KEY_2 = "other_key_2";
    private static final String TEST_VALUE_2 = "other_value_2";
    private static final long PROPERTIES_DEFINED_DURATION = 38;
    private static final TimeUnit PROPERTIES_DEFINED_TIME_UNIT = TimeUnit.HOURS;

    private ConcurrentHashMapTaggableCacheManager concurrentHashMapTaggableCacheManager;
    private Map<String, Object> contentStore;
    private Map<String, LocalDateTime> contentStoreTtl;
    private Map<String, List<String>> tagsToKeyMap;
    private ScheduledExecutorService scheduledExecutorServiceMock;
    private Lock lockMock;

    @BeforeEach
    void setup() {
        contentStore = new HashMap<>();
        contentStoreTtl = new HashMap<>();
        tagsToKeyMap = new HashMap<>();
        scheduledExecutorServiceMock = mock(ScheduledExecutorService.class);
        lockMock = mock(Lock.class);
        ConcurrentHashMapTaggableProperties concurrentHashMapTaggableProperties = new ConcurrentHashMapTaggableProperties(PROPERTIES_DEFINED_DURATION, PROPERTIES_DEFINED_TIME_UNIT);
        concurrentHashMapTaggableCacheManager = new ConcurrentHashMapTaggableCacheManager(
                contentStore, contentStoreTtl, tagsToKeyMap, scheduledExecutorServiceMock, lockMock, concurrentHashMapTaggableProperties
        );
    }

    @Test
    void autoKeyWipe_should_work() throws NoSuchMethodException {
        var expiredKey = "expiredShit";
        var nonExpiredKey = "nonExpiredShit";
        contentStore.put(expiredKey, 12);
        contentStore.put(nonExpiredKey, 14);
        contentStoreTtl.put(expiredKey, LocalDateTime.now().minusHours(5));
        contentStoreTtl.put(nonExpiredKey, LocalDateTime.now().plusHours(5));
        List<String> tagContent = new ArrayList<>(List.of(expiredKey, nonExpiredKey));
        tagsToKeyMap.put(TEST_TAG, tagContent);

        concurrentHashMapTaggableCacheManager.autoKeyWipe();

        assertThat(ConcurrentHashMapTaggableCacheManager.class.getMethod("autoKeyWipe").getAnnotation(PostConstruct.class))
                .isNotNull();
        var captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorServiceMock, times(1)).scheduleAtFixedRate(
                captor.capture(), eq(PROPERTIES_DEFINED_DURATION), eq(PROPERTIES_DEFINED_DURATION), eq(PROPERTIES_DEFINED_TIME_UNIT)
        );
        captor.getValue().run();
        verify(lockMock, times(1)).lock();
        assertThat(contentStore)
                .hasSize(1)
                .containsKey(nonExpiredKey)
                .doesNotContainKey(expiredKey);
        assertThat(contentStoreTtl)
                .hasSize(1)
                .containsKey(nonExpiredKey)
                .doesNotContainKey(expiredKey);
        assertThat(tagsToKeyMap.get(TEST_TAG))
                .hasSize(1)
                .contains(nonExpiredKey);
        verify(lockMock, times(1)).unlock();
    }

    @Test
    void keyExists_should_work() {
        assertThat(concurrentHashMapTaggableCacheManager.keyExists(TEST_KEY)).isFalse();
        contentStore.put(TEST_KEY, "val");
        assertThat(concurrentHashMapTaggableCacheManager.keyExists(TEST_KEY)).isTrue();
    }

    @Test
    void findByKey_should_work() {
        contentStore.put(TEST_KEY, TEST_VALUE);
        assertThat(concurrentHashMapTaggableCacheManager.findByKey(TEST_KEY)).isEqualTo(TEST_VALUE);
    }

    @Test
    void evictByCacheTag_should_work() {
        contentStore.put(TEST_KEY, TEST_VALUE);
        concurrentHashMapTaggableCacheManager.evictByCacheTag(TEST_TAG);
        assertThat(contentStore).containsKey(TEST_KEY);
        List<String> list = new ArrayList<>();
        list.add(TEST_KEY);
        tagsToKeyMap.put(TEST_TAG, list);

        concurrentHashMapTaggableCacheManager.evictByCacheTag(TEST_TAG);

        verify(lockMock, times(2)).lock();
        verify(lockMock, times(2)).unlock();
        assertThat(contentStore).doesNotContainKey(TEST_KEY);
        assertThat(tagsToKeyMap.get(TEST_TAG)).isEmpty();
    }

    @Test
    void saveEntry_should_throw_when_tried_to_add_and_existing_key() {
        contentStore.put(TEST_KEY, TEST_VALUE);
        List<String> list = List.of();

        assertThatThrownBy(() -> concurrentHashMapTaggableCacheManager.saveEntry(TEST_KEY, TEST_VALUE, list))
                .isInstanceOf(IllegalStateException.class);
        verify(lockMock, times(1)).lock();
        verify(lockMock, times(1)).unlock();
    }

    @Test
    void saveEntry_should_work() {
        var otherKey = "other_key";

        concurrentHashMapTaggableCacheManager.saveEntry(TEST_KEY, TEST_VALUE, List.of(TEST_TAG));
        concurrentHashMapTaggableCacheManager.saveEntry(otherKey, "other_value", List.of(TEST_TAG));

        assertThat(contentStore)
                .containsKey(TEST_KEY)
                .containsEntry(TEST_KEY, TEST_VALUE);
        assertThat(contentStoreTtl)
                .containsKey(TEST_KEY);
        assertThat(contentStoreTtl.get(TEST_KEY)).isAfter(LocalDateTime.now().plusHours(PROPERTIES_DEFINED_DURATION - 1));
        assertThat(tagsToKeyMap)
                .containsKey(TEST_TAG)
                .containsEntry(TEST_TAG, List.of(TEST_KEY, otherKey));
        verify(lockMock, times(2)).lock();
        verify(lockMock, times(2)).unlock();
    }

    @Test
    void saveEntry_and_findByKey_should_accept_null_value() {
        concurrentHashMapTaggableCacheManager.saveEntry(TEST_KEY, null, List.of());
        assertThat(concurrentHashMapTaggableCacheManager.keyExists(TEST_KEY)).isTrue();
        assertThat(concurrentHashMapTaggableCacheManager.findByKey(TEST_KEY)).isNull();
    }

    @Test
    void evictByKey_should_work() {
        contentStore.put(TEST_KEY, TEST_VALUE);
        contentStore.put(TEST_KEY_2, TEST_VALUE_2);
        contentStoreTtl.put(TEST_KEY, LocalDateTime.now());
        tagsToKeyMap.put(TEST_TAG, new ArrayList<>(List.of(TEST_KEY, TEST_KEY_2)));

        concurrentHashMapTaggableCacheManager.evictByKey(TEST_KEY);

        assertThat(contentStore)
                .doesNotContainKey(TEST_KEY)
                .containsKey(TEST_KEY_2);
        assertThat(contentStoreTtl)
                .doesNotContainKey(TEST_KEY);
        verify(lockMock, times(1)).lock();
        verify(lockMock, times(1)).unlock();
        assertThat(tagsToKeyMap.get(TEST_TAG)).hasSize(2);
        var captor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorServiceMock, times(1)).execute(captor.capture());
        captor.getValue().run();
        assertThat(tagsToKeyMap.get(TEST_TAG))
                .hasSize(1)
                .contains(TEST_KEY_2);
        verify(lockMock, times(2)).lock();
        verify(lockMock, times(2)).unlock();
    }

}
