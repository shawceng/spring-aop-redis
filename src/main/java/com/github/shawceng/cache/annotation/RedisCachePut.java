package com.github.shawceng.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存更新
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCachePut {

    int DEFAULT_TTL = 4 * 60 * 60;

    String value();

    int[] key_args() default {};

    int ttl() default DEFAULT_TTL;
}
