package com.kevinguanchedarias.taggablecache.aspect;

import com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager;
import com.kevinguanchedarias.taggablecache.placeholderresolver.SpringSpelPlaceholderResolver;
import com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel.FAKE_PLAIN_KEY;
import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel.FAKE_PLAIN_TAG;
import static com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel.KNOWN_RETURN_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                TaggableCacheableAspect.class,
                AnnotatedFakeClassWithSpringSpel.class,
                SpelExpressionParser.class,
                SpringSpelPlaceholderResolver.class
        }
)
@MockBean(TaggableCacheManager.class)
@EnableAspectJAutoProxy
class TaggableCacheableAspectTest {
    private static final String KNOWN_AUTO_GENERATED_KEY_1 = "class com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel_doSomethingWithDefaultKey";
    private static final String KNOWN_AUTO_GENERATED_KEY_2 = "class com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel_doSomethingWithDefaultKeyAndArgs_FooFirst_BarSecond";
    private static final String KNOWN_AUTO_GENERATED_SUFFIXED_KEY = "class com.kevinguanchedarias.taggablecache.test.AnnotatedFakeClassWithSpringSpel_doSomethingWithKeySuffix_12";
    private static final String KNOWN_CACHED_VALUE = "IsCached";
    private static final String FIRST_ARG = "FooFirst";
    private static final String SECOND_ARG = "BarSecond";

    private final AnnotatedFakeClassWithSpringSpel annotatedFakeClassWithSpringSpel;
    private final TaggableCacheManager taggableCacheManager;

    @Autowired
    TaggableCacheableAspectTest(
            AnnotatedFakeClassWithSpringSpel annotatedFakeClassWithSpringSpel,
            TaggableCacheManager taggableCacheManager
    ) {
        this.annotatedFakeClassWithSpringSpel = annotatedFakeClassWithSpringSpel;
        this.taggableCacheManager = taggableCacheManager;
    }


    @Test
    void handleTaggableCacheAnnotation_should_work_with_default_key_and_not_spel_tags_and_cache_it_if_not() {
        var result = annotatedFakeClassWithSpringSpel.doSomethingWithDefaultKey();

        verify(taggableCacheManager, times(1)).keyExists(KNOWN_AUTO_GENERATED_KEY_1);
        verify(taggableCacheManager, never()).findByKey(any());
        verify(taggableCacheManager, times(1)).saveEntry(KNOWN_AUTO_GENERATED_KEY_1, KNOWN_RETURN_VALUE, List.of(FAKE_PLAIN_TAG));
        assertThat(result).isEqualTo(KNOWN_RETURN_VALUE);
    }

    @Test
    void handleTaggableCacheAnnotation_should_work_with_default_key_and_not_spel_tags_and_return_cached_value() {
        given(taggableCacheManager.keyExists(KNOWN_AUTO_GENERATED_KEY_1)).willReturn(true);
        given(taggableCacheManager.findByKey(KNOWN_AUTO_GENERATED_KEY_1)).willReturn(KNOWN_CACHED_VALUE);

        var result = annotatedFakeClassWithSpringSpel.doSomethingWithDefaultKey();

        verify(taggableCacheManager, times(1)).keyExists(KNOWN_AUTO_GENERATED_KEY_1);
        verify(taggableCacheManager, times(1)).findByKey(any());
        verify(taggableCacheManager, never()).saveEntry(any(), any(), anyList());
        assertThat(result).isEqualTo(KNOWN_CACHED_VALUE);
    }

    @Test
    void handleTaggableCacheAnnotation_should_work_with_default_key_and_method_with_args() {
        given(taggableCacheManager.keyExists(KNOWN_AUTO_GENERATED_KEY_2)).willReturn(true);
        given(taggableCacheManager.findByKey(KNOWN_AUTO_GENERATED_KEY_2)).willReturn(KNOWN_CACHED_VALUE);

        var result = annotatedFakeClassWithSpringSpel.doSomethingWithDefaultKeyAndArgs(FIRST_ARG, SECOND_ARG);

        verify(taggableCacheManager, times(1)).keyExists(KNOWN_AUTO_GENERATED_KEY_2);
        verify(taggableCacheManager, times(1)).findByKey(KNOWN_AUTO_GENERATED_KEY_2);
        assertThat(result).isEqualTo(KNOWN_CACHED_VALUE);
    }

    @Test
    void handleTaggableCacheAnnotation_should_use_plain_key_and_plain_tag() {
        var result = annotatedFakeClassWithSpringSpel.doSomethingWithPlainThings();

        verify(taggableCacheManager, times(1)).keyExists(FAKE_PLAIN_KEY);
        assertThat(result).isEqualTo(KNOWN_RETURN_VALUE);
    }

    @Test
    void handleTaggableCacheAnnotation_should_use_key_with_suffix_and_plain_tag() {
        var result = annotatedFakeClassWithSpringSpel.doSomethingWithKeySuffix(12);
        verify(taggableCacheManager, times(1)).keyExists(KNOWN_AUTO_GENERATED_SUFFIXED_KEY);
        assertThat(result).isEqualTo(KNOWN_RETURN_VALUE);
    }

    @Test
    void handleTaggableCacheAnnotation_should_use_spel_for_custom_key_and_tags() {
        var expectedKey = "a_key:doSomethingWithCustomKeyAndSpelTags";
        var expectedTag = "tagKey:4";
        var result = annotatedFakeClassWithSpringSpel.doSomethingWithCustomKeyAndSpelTags(4);

        verify(taggableCacheManager, times(1)).keyExists(expectedKey);
        verify(taggableCacheManager, times(1)).saveEntry(expectedKey, KNOWN_RETURN_VALUE, List.of(expectedTag));
        assertThat(result).isEqualTo(KNOWN_RETURN_VALUE);
    }
}
