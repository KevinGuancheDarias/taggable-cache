package com.kevinguanchedarias.taggablecache.aspect;

import com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager;
import com.kevinguanchedarias.taggablecache.placeholderresolver.DefaultPlaceholderResolver;
import com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithDefaultPlaceholder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithDefaultPlaceholder.FAKE_PLAIN_KEY;
import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithDefaultPlaceholder.FAKE_PLAIN_TAG;
import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithDefaultPlaceholder.KNOWN_RETURN_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                TaggableCacheEvictAspect.class,
                AnnotatedFakeClassWithDefaultPlaceholder.class,
                DefaultPlaceholderResolver.class
        }
)
@MockBean(TaggableCacheManager.class)
@EnableAspectJAutoProxy
class TaggableCacheEvictAspectTest {
    private final AnnotatedFakeClassWithDefaultPlaceholder annotatedFakeClassWithDefaultPlaceholder;
    private final TaggableCacheManager taggableCacheManager;

    @Autowired
    public TaggableCacheEvictAspectTest(
            AnnotatedFakeClassWithDefaultPlaceholder annotatedFakeClassWithDefaultPlaceholder,
            TaggableCacheManager taggableCacheManager
    ) {
        this.annotatedFakeClassWithDefaultPlaceholder = annotatedFakeClassWithDefaultPlaceholder;
        this.taggableCacheManager = taggableCacheManager;
    }

    @Test
    void evictByKey_should_work_with_plain_key() {
        assertThat(annotatedFakeClassWithDefaultPlaceholder.byPlainKey()).isEqualTo(KNOWN_RETURN_VALUE);
        verify(taggableCacheManager, times(1)).evictByKey(FAKE_PLAIN_KEY);
    }

    @Test
    void evictByKey_should_work_with_placeholder_keys() {
        var key = "fooKey";
        assertThat(annotatedFakeClassWithDefaultPlaceholder.byPlaceholderKey(key)).isEqualTo(KNOWN_RETURN_VALUE);
        verify(taggableCacheManager, times(1)).evictByKey("key_fooKey");
    }

    @Test
    void evictByTag_should_work_with_plain() {
        assertThat(annotatedFakeClassWithDefaultPlaceholder.byPlainTag()).isEqualTo(KNOWN_RETURN_VALUE);
        verify(taggableCacheManager, times(1)).evictByCacheTag(FAKE_PLAIN_TAG);
    }

    @Test
    void evictByTag_should_work_with_placeholder() {
        int tagValue = 18;
        assertThat(annotatedFakeClassWithDefaultPlaceholder.byTag(tagValue)).isEqualTo(KNOWN_RETURN_VALUE);
        verify(taggableCacheManager, times(1)).evictByCacheTag("tag:18");
    }
}
