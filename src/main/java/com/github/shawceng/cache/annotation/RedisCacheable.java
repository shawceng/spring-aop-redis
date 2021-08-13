package com.github.shawceng.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存get or put
 * 使用: @RedisCacheable(value="CACHE:{id}{name}")
 * '{field}'会被格式化为切点方法参数中的属性，
 * 比如切点第一个参数为user，{id}会被格式化为该user的id值
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCacheable {
    /**
     * 默认过期时间
      */
    int DEFAULT_TTL = 4 * 60 * 60;

    /**
     *  NULL值过期时间
      */
    int NULL_TTL = 5 * 60;

    /**
     * 要存储的key值
     */
    String value();

    /**
     * 注解方法传入参数顺序
     */
    int[] key_args() default {};

    /**
     * 是否穿透缓存
     */
    RedisAction action() default RedisAction.REDIS_FIRST;

    int ttl() default DEFAULT_TTL;

    boolean sync() default false;

    boolean cacheNull() default true;

    int nullTtl() default NULL_TTL;

    enum RedisAction {
        REDIS_FIRST,
        REDIS_STAB
    }
}

