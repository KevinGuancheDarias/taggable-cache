package com.kevinguanchedarias.taggablecache.configuration.properties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties("com.kevinguanchedarias.taggable-cache.concurrent-hash-map")
@ConstructorBinding
@Value
public class ConcurrentHashMapTaggableProperties {
    Long cacheTtl;
    TimeUnit timeUnit;

    public ConcurrentHashMapTaggableProperties(
            @DefaultValue("24") Long cacheTtl,
            @DefaultValue("HOURS") TimeUnit timeUnit
    ) {
        this.cacheTtl = cacheTtl;
        this.timeUnit = timeUnit;
    }
}
