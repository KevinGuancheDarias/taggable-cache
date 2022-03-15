package com.kevinguanchedarias.taggablecache.util;

import lombok.experimental.UtilityClass;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

@UtilityClass
public class AspectUtils {
    public static <T extends Annotation> T findAnnotation(JoinPoint joinPoint, Class<T> clazz) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(clazz);
    }
}
