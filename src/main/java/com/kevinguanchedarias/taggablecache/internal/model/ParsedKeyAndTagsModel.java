package com.kevinguanchedarias.taggablecache.internal.model;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;

@Builder
@Value
public class ParsedKeyAndTagsModel {
    String key;
    Collection<String> tags;
}
