package com.kevinguanchedarias.taggablecache.aspect;

import com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager;
import com.kevinguanchedarias.taggablecache.placeholderresolver.PlaceholderResolver;
import com.kevinguanchedarias.taggablecache.util.AspectUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Listens for {@link TaggableCacheable} annotations
 *
 * @since 0.1.0
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class TaggableCacheableAspect {

    private final TaggableCacheManager taggableCacheManager;
    private final PlaceholderResolver placeholderResolver;

    @Around("@annotation(com.kevinguanchedarias.taggablecache.aspect.TaggableCacheable)")
    public Object handleTaggableCacheAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        var annotation = AspectUtils.findAnnotation(joinPoint, TaggableCacheable.class);
        var tags = List.of(annotation.tags());
        var key = annotation.key();
        if (!StringUtils.hasLength(key)) {
            key = generateDefaultKeyExpression((MethodSignature) joinPoint.getSignature());
        }
        var parsedKeyAndTags = placeholderResolver.resolveExpressions(joinPoint, key, tags);

        if (taggableCacheManager.keyExists(parsedKeyAndTags.getKey())) {
            log.debug("Cache HIT for key {}", parsedKeyAndTags.getKey());
            return this.taggableCacheManager.findByKey(parsedKeyAndTags.getKey());
        } else {
            log.debug("Cache MISS for key {}", parsedKeyAndTags.getKey());
            var executionResult = joinPoint.proceed();
            taggableCacheManager.saveEntry(parsedKeyAndTags.getKey(), executionResult, parsedKeyAndTags.getTags());
            return executionResult;
        }
    }

    private String generateDefaultKeyExpression(MethodSignature methodSignature) {
        var keyExpression = "#className + \"_\" + #methodName";
        var parameterNames = methodSignature.getParameterNames();
        if (parameterNames.length == 0) {
            return keyExpression;
        } else {
            keyExpression += "+";
            var argumentsAsExpressionVariables = List.of(parameterNames)
                    .stream()
                    .reduce("", (buffer, currentArg) -> buffer += "\"_\" + #" + currentArg + "+");
            return keyExpression + argumentsAsExpressionVariables.substring(0, argumentsAsExpressionVariables.length() - 1);
        }
    }
}
