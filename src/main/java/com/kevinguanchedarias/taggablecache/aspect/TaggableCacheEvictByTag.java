package com.kevinguanchedarias.taggablecache.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Evicts cache by tags when the annotated method returns without exception
 *
 * @since 0.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaggableCacheEvictByTag {
    String[] tags();
}
