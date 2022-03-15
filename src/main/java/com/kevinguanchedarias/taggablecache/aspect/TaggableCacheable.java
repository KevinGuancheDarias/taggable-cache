package com.kevinguanchedarias.taggablecache.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaggableCacheable {
    /**
     * Tags to use SpelExpressions are supported <br>
     * Example: For method foo(user) a valid tag would be "user:#{user.id}"
     */
    String[] tags();

    /**
     * If specified will be used as key, SpelExpressions are supported with method arguments <br>
     * Example: for method signature foo(user, data) a valid expression would be "#{methodName}_#{user.name}_#{data} <br>
     * If not specified, or blank string, default key is "clazzName_methodName_arg1toString_arg2toString_...."
     *
     * @since 0.1.0
     */
    String key() default "";
}
