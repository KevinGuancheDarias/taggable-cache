package com.kevinguanchedarias.taggablecache.placeholderresolver;

import com.kevinguanchedarias.taggablecache.internal.model.ParsedKeyAndTagsModel;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * Implementations to resolve placeholder expressions
 *
 * @since 0.1.0
 */
public interface PlaceholderResolver {
    /**
     * Resolves the expressions in the key and the tags <br>
     * Implementations must resolve className, methodName, and method arguments
     *
     * @since 0.1.0
     */
    ParsedKeyAndTagsModel resolveExpressions(JoinPoint joinPoint, String key, List<String> tags);
}
