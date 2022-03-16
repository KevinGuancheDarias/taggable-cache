package com.kevinguanchedarias.taggablecache.helper;

import lombok.AllArgsConstructor;

/**
 * Allows to ease the programmatic tag management (using directly {@link com.kevinguanchedarias.taggablecache.manager.TaggableCacheManager}) <br>
 * By providing methods to build the common tags
 *
 * @since 0.1.1
 */
@AllArgsConstructor
public class TagHelper {
    private String tagName;

    /**
     * @return tag + ":list"
     * @since 0.1.1
     */
    public String list() {
        return plainPart("list");
    }

    /**
     * @return tag + ":" + part
     * @since 0.1.1
     */
    public String plainPart(Object part) {
        return tagName + ":" + part;
    }

    /**
     * @return tag + ":#" + part
     * @since 0.1.1
     */
    public String varPart(String part) {
        return tagName + ":#" + part;
    }
}
