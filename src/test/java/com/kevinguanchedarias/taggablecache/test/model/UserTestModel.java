package com.kevinguanchedarias.taggablecache.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class UserTestModel {
    String name;
    int age;
}
