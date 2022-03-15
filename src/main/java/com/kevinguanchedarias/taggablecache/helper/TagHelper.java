package com.kevinguanchedarias.taggablecache.helper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TagHelper {
    private String tagName;

    public String list() {
        return plainPart("list");
    }

    public String plainPart(Object part) {
        return tagName + ":" + part;
    }

    public String varPart(String part) {
        return tagName + ":#" + part;
    }
}
