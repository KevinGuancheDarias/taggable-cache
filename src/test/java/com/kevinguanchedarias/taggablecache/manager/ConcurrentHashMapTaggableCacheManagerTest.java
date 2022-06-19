package com.kevinguanchedarias.taggablecache.manager;

import com.kevinguanchedarias.taggablecache.configuration.properties.ConcurrentHashMapTaggableProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(OutputCaptureExtension.class)
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
    private Map<String, Set<String>> tagsToKeyMap;
    private ScheduledExecutorService scheduledExecutorServiceMock;
    private Lock lockMock;

    @BeforeEach
    void setup() {
        contentStore = spy(new HashMap<>());
        contentStoreTtl = spy(new HashMap<>());
        tagsToKeyMap = spy(new HashMap<>());
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
        Set<String> tagContent = new HashSet<>(List.of(expiredKey, nonExpiredKey));
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
        Set<String> list = new HashSet<>();
        list.add(TEST_KEY);
        tagsToKeyMap.put(TEST_TAG, list);

        concurrentHashMapTaggableCacheManager.evictByCacheTag(TEST_TAG);

        verify(lockMock, times(2)).lock();
        verify(lockMock, times(2)).unlock();
        assertThat(contentStore).doesNotContainKey(TEST_KEY);
        assertThat(tagsToKeyMap.get(TEST_TAG)).isEmpty();
    }

    @Test
    void evictByCacheTag_with_part_should_work() {
        var computedTag = "user:8";
        var tag = "user";
        int tagPart = 8;
        contentStore.put(TEST_KEY, TEST_VALUE);
        concurrentHashMapTaggableCacheManager.evictByCacheTag(tag, tagPart);
        assertThat(contentStore).containsKey(TEST_KEY);
        Set<String> list = new HashSet<>();
        list.add(TEST_KEY);
        tagsToKeyMap.put(computedTag, list);

        concurrentHashMapTaggableCacheManager.evictByCacheTag(tag, tagPart);

        verify(lockMock, times(2)).lock();
        verify(lockMock, times(2)).unlock();
        assertThat(contentStore).doesNotContainKey(TEST_KEY);
        assertThat(tagsToKeyMap.get(computedTag)).isEmpty();
    }

    @Test
    void saveEntry_should_log_when_tried_to_add_and_existing_key(CapturedOutput capturedOutput) {
        contentStore.put(TEST_KEY, TEST_VALUE);
        List<String> list = List.of();

        concurrentHashMapTaggableCacheManager.saveEntry(TEST_KEY, TEST_VALUE, list);

        verify(lockMock, times(1)).lock();
        verify(lockMock, times(1)).unlock();
        assertThat(capturedOutput.getOut()).contains("Tried to update a key that is already stored");
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
                .containsEntry(TEST_TAG, Set.of(TEST_KEY, otherKey));
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
        tagsToKeyMap.put(TEST_TAG, new HashSet<>(List.of(TEST_KEY, TEST_KEY_2)));

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

    @Test
    void clear_should_work() {
        concurrentHashMapTaggableCacheManager.clear();

        verify(lockMock, times(1)).lock();
        verify(contentStore, times(1)).clear();
        verify(contentStoreTtl, times(1)).clear();
        verify(tagsToKeyMap, times(1)).clear();
        verify(lockMock, times(1)).unlock();
    }

    @Test
    void clear_should_unlock_even_on_exception() {
        doThrow(new IllegalStateException()).when(tagsToKeyMap).clear();

        assertThatThrownBy(() -> concurrentHashMapTaggableCacheManager.clear())
                .isInstanceOf(IllegalStateException.class);

        verify(lockMock, times(1)).unlock();
    }

}
