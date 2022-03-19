# Taggable Cache

## Boring Introduction

Due to the lack of support from Spring (as far as I was able to find), to a "taggable cache" which is inspired in Drupal's cache core. I have decided to implement my own one... For now is :warning: **NOT READY FOR PRODUCTION** :warning: , use with caution

## Usage

First of all add to your pom the dependency
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    ....
    <dependency>
        <groupId>com.github.KevinGuancheDarias</groupId>
        <artifactId>taggable-cache</artifactId>
        <version>v0.1.2</version>
    </dependency>
</dependencies>
```

In any of your Spring Configurations beans Import the default Configuration, for Example:

```java
@SpringBootApplication()
@Import(TaggableCacheDefaultConfiguration.class)
public class Application { }
```

After that you should be able to use the annotations `@TaggableCacheable`, `@TaggableCacheEvictByKey` and `@TaggableCacheEvictByTag`

Example usage of the annotations:

```java
@Service
public class UserService {
    ...
    @TaggableCacheEvictByTag(tags = "user:#id")
    public User saveUser(int id) {
        ...
    }
}

@Service
public class UserMassiveOperations {
    @TaggableCacheable(tags = {"user:#userId", "world_conquer:list"})
    public void heavyThing(int userId) {
        serviceA.doHeavy();
        worldConquerService.saveNew(...);
        ...
    }

}
```

Example usage of the programmatic way

```java
@Service
@AllArgsConstructor
public class UserService {
    public static final TagHelper TAG_HELPER = new TagHelper("user");
    private final TaggableCacheManager taggableCacheManager;

    public User saveUser(int id) {
        ...
        taggableCacheManager.evictByCacheTag(TAG_HELPER.plainPart(id));
    }
}

@Service
public class UserMassiveOperations {
    private final TaggableCacheManager taggableCacheManager;

    public void heavyThing(int userId) {
        var cacheKey = "heavyThing_user_" + userId;
        if(!taggableCacheManager.keyExists(cacheKey)) {
            serviceA.doHeavy();
            worldConquerService.saveNew(...);
            ...
            taggableCacheManager.saveEntry(
                cacheKey, 
                null, 
                List.of(UserService.TAG_HELPER.plainPart(userId), WorldConquerService.TAG_HELPER.list())
            );
        }
    }

}
```

## Create your own configuration
Just see how the current works [TaggableCacheDefaultConfiguration](https://github.com/KevinGuancheDarias/taggable-cache/blob/v0.1.2/src/main/java/com/kevinguanchedarias/taggablecache/configuration/TaggableCacheDefaultConfiguration.java)

You can switch the used annotation `PlaceholderResolver`, default is `DefaultPlaceholderResolver` (which translate using hash replacement) for example `"user:#id"` would be translate to `user:6`, for `SpringSpelPlaceholderResolver` this would do the same translation `"\"user:\" + #id"` ... While Spring one is way superior (supports method invocation, statics, and more) is not the default as most times the cache is going to be entity:id

## Future

### Add the concept of "cache contexts"
Just as Drupal has the cache contexts, would be something interesting there, so a cached entry may depend on something, for example the logged in user @TaggableCacheable(contexts = "user"), this is something that I have to give a better think on how to implement that

### Migrating to Spring CacheManager
A question to StackOverflow was added regarding this pattern in Spring, [link](https://stackoverflow.com/a/71490767/1922558) the user [John Blum](https://stackoverflow.com/users/3390417/john-blum) added a nice answer, of how to extend the current Spring Cache ecosystem for my use case... A future rewrite may delegate the caching in the Spring's CacheManager instead of implementing, owns way, as in some years I may need to use Redis for caching a distributed backend... an instead of implementing my own RedisCacheManager, I can use the Spring's one... For now the project will continue to use its own CacheManager, not to worry about the rewrite as the new cache managers delegating to Spring will implement the existing TaggableCacheManager.
