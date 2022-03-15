package com.kevinguanchedarias.taggablecache.configuration;

import com.kevinguanchedarias.taggablecache.aspect.TaggableCacheEvictAspect;
import com.kevinguanchedarias.taggablecache.aspect.TaggableCacheableAspect;
import com.kevinguanchedarias.taggablecache.configuration.properties.ConcurrentHashMapTaggableProperties;
import com.kevinguanchedarias.taggablecache.manager.ConcurrentHashMapTaggableCacheManager;
import com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager;
import com.kevinguanchedarias.taggablecache.placeholderresolver.DefaultPlaceholderResolver;
import com.kevinguanchedarias.taggablecache.placeholderresolver.PlaceholderResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(ConcurrentHashMapTaggableProperties.class)
public class TaggableCacheDefaultConfiguration {

    @Bean
    public TaggableCacheManager taggableCacheManager(ConcurrentHashMapTaggableProperties concurrentHashMapTaggableProperties) {
        return new ConcurrentHashMapTaggableCacheManager(concurrentHashMapTaggableProperties);
    }

    @Bean
    public PlaceholderResolver placeholderResolver() {
        return new DefaultPlaceholderResolver();
    }

    @Bean
    public TaggableCacheableAspect taggableCacheableAspect(
            TaggableCacheManager taggableCacheManager,
            PlaceholderResolver placeholderResolver
    ) {
        return new TaggableCacheableAspect(taggableCacheManager, placeholderResolver);
    }

    @Bean
    public TaggableCacheEvictAspect taggableCacheEvictAspect(
            TaggableCacheManager taggableCacheManager,
            PlaceholderResolver placeholderResolver
    ) {
        return new TaggableCacheEvictAspect(taggableCacheManager, placeholderResolver);
    }
}
