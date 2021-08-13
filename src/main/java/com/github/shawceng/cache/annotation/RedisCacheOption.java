package com.github.shawceng.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 复合操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCacheOption {
    RedisCacheable[] cacheable() default {};

    RedisCachePut[] cachePut() default {};

    RedisCacheEvict[] cacheEvict() default {};
}
