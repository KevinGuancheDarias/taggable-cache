package com.kevinguanchedarias.taggablecache.placeholderresolver;

import com.kevinguanchedarias.taggablecache.internal.model.ParsedKeyAndTagsModel;
import org.aspectj.lang.JoinPoint;

import java.util.List;

public interface PlaceholderResolver {
    ParsedKeyAndTagsModel resolveExpressions(JoinPoint joinPoint, String key, List<String> tags);
}
