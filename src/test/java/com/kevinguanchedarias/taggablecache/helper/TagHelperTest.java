package com.kevinguanchedarias.taggablecache.helper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagHelperTest {
    @Test
    void list_should_work() {
        var tag = "tag";
        assertThat(new TagHelper(tag).list()).isEqualTo("tag:list");
    }

    @Test
    void plainPart_should_work() {
        var tag = "foo";
        int part = 8;
        assertThat(new TagHelper(tag).plainPart(part)).isEqualTo("foo:8");
    }

    @Test
    void varPart_should_work() {
        var tag = "bar";
        var part = "id";
        assertThat(new TagHelper(tag).varPart(part)).isEqualTo("bar:#id");
    }
}
