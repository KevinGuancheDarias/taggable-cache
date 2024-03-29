package com.kevinguanchedarias.taggablecache.aspect;

import com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager;
import com.kevinguanchedarias.taggablecache.placeholderresolver.PlaceholderResolver;
import com.kevinguanchedarias.taggablecache.util.AspectUtils;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listens for {@link TaggableCacheEvictByTag} annd {@link TaggableCacheEvictByKey} annotations
 *
 * @since 0.1.0
 */
@Aspect
@Component
@AllArgsConstructor
public class TaggableCacheEvictAspect {

    private final TaggableCacheManager taggableCacheManager;
    private final PlaceholderResolver placeholderResolver;

    @Before("@annotation(com.kevinguanchedarias.taggablecache.aspect.TaggableCacheEvictByKey)")
    void evictByKey(JoinPoint joinPoint) {
        var annotation = AspectUtils.findAnnotation(joinPoint, TaggableCacheEvictByKey.class);
        taggableCacheManager.evictByKey(placeholderResolver.resolveExpressions(joinPoint, annotation.key(), List.of()).getKey());
    }

    @Before("@annotation(com.kevinguanchedarias.taggablecache.aspect.TaggableCacheEvictByTag)")
    void evictByTags(JoinPoint joinPoint) {
        var annotation = AspectUtils.findAnnotation(joinPoint, TaggableCacheEvictByTag.class);
        var tags = List.of(annotation.tags());
        placeholderResolver.resolveExpressions(joinPoint, "", tags).getTags()
                .forEach(taggableCacheManager::evictByCacheTag);
    }
}
