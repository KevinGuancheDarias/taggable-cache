package com.kevinguanchedarias.taggablecache.test;

import com.kevinguanchedarias.taggablecache.aspect.TaggableCacheable;
import org.springframework.stereotype.Service;

@Service
public class AnnotatedFakeClassWithSpringSpel {
    public static final String FAKE_PLAIN_TAG = "foo-tag";
    public static final String KNOWN_RETURN_VALUE = "theValue";
    public static final String FAKE_PLAIN_KEY = "theKey";
    public static final String FAKE_PLAIN_SUFFIX = "#keySuffixArg";
    public static final String KEY_WITH_SPEL = "\"a_key:\" + #methodName";
    public static final String TAG_WITH_SPEL = "\"tagKey:\" + #tagValueArg";

    @TaggableCacheable(tags = FAKE_PLAIN_TAG)
    public String doSomethingWithDefaultKey() {
        System.out.println("content invoked");
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheable(tags = FAKE_PLAIN_TAG)
    public String doSomethingWithDefaultKeyAndArgs(String first, String second) {
        System.out.println("content invoked " + first + "_" + second);
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheable(key = FAKE_PLAIN_KEY, tags = FAKE_PLAIN_TAG)
    public String doSomethingWithPlainThings() {
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheable(keySuffix = FAKE_PLAIN_SUFFIX, tags = FAKE_PLAIN_TAG)
    public String doSomethingWithKeySuffix(int keySuffixArg) {
        return KNOWN_RETURN_VALUE;
    }

    @TaggableCacheable(key = KEY_WITH_SPEL, tags = TAG_WITH_SPEL)
    public String doSomethingWithCustomKeyAndSpelTags(int tagValueArg) {
        return KNOWN_RETURN_VALUE;
    }
}
