package com.kevinguanchedarias.taggablecache.test;

import com.kevinguanchedarias.taggablecache.aspect.TaggableCacheEvictByKey;
import com.kevinguanchedarias.taggablecache.aspect.TaggableCacheEvictByTag;
import org.springframework.stereotype.Service;

@Service
public class AnnotatedFakeClassWithDefaultPlaceholder {
    public static final String KNOWN_RETURN_VALUE = "theVal";
    public static final String FAKE_PLAIN_KEY = "theKey";
    public static final String FAKE_PLAIN_TAG = "foo-tag";
    public static final String KEY_WITH_PLACEHOLDER = "key_#key";
    public static final String TAG_WITH_PLACEHOLDER = "tag:#id";

    @TaggableCacheEvictByKey(key = FAKE_PLAIN_KEY)
    public String byPlainKey() {
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheEvictByKey(key = KEY_WITH_PLACEHOLDER)
    public String byPlaceholderKey(String key) {
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheEvictByTag(tags = FAKE_PLAIN_TAG)
    public String byPlainTag() {
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheEvictByTag(tags = TAG_WITH_PLACEHOLDER)
    public String byTag(int id) {
        return KNOWN_RETURN_VALUE;
    }
}
