package com.github.shawceng.cache;

import com.github.shawceng.cache.annotation.RedisCacheEvict;
import com.github.shawceng.cache.annotation.RedisCacheOption;
import com.github.shawceng.cache.annotation.RedisCachePut;
import com.github.shawceng.cache.annotation.RedisCacheable;
import com.github.shawceng.utility.PlaceholderResolver;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * AOP切面组件
 * 绑定注解功能
 */
@Component
@Aspect
public class RedisAopAdviceDefine {

    private Logger logger = LoggerFactory.getLogger(RedisAopAdviceDefine.class);

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    /**
     * RedisCacheable注解实现
     * @param joinPoint 切点，也就是加入注解的方法
     * @param redisCacheable 绑定到该切点的注解
     * @return 切点方法返回值
     * @throws Throwable
     */
    @Around("@annotation(redisCacheable)")
    public Object cacheSet(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        boolean stab = redisCacheable.action() != RedisCacheable.RedisAction.REDIS_FIRST;
        Object[] keyArgs = getKeyArgs(joinPoint.getArgs(), redisCacheable.key_args());

        // 采用反射格式化注解value
        String key = PlaceholderResolver.resolveByObjects(redisCacheable.value(), keyArgs);

        Class<?> returnType = method.getReturnType();
        // 是否穿透
        Object result = stab ? null : get(key, returnType);
        if (result != null) {
            return result;
        }

        result = joinPoint.proceed();
        if (result != null) {
            redisTemplate.opsForValue().set(key, (Serializable) result, redisCacheable.ttl(), TimeUnit.SECONDS);
        }
        return result;
    }

    /**
     * RedisCachePut注解的实现
     * @param joinPoint 切点
     * @param redisCachePut 该切点绑定的注解
     * @param retObj 该切点返回的结果
     */
    @AfterReturning(value = "@annotation(redisCachePut)", returning = "retObj")
    public void cachePut(JoinPoint joinPoint, RedisCachePut redisCachePut, Object retObj) {
        Object[] keyArgs = getKeyArgs(joinPoint.getArgs(), redisCachePut.key_args());

        String key = PlaceholderResolver.resolveByObjects(redisCachePut.value(), keyArgs);

        if (retObj!= null) {
            redisTemplate.opsForValue().set(key, (Serializable) retObj, redisCachePut.ttl(), TimeUnit.SECONDS);
        }
    }

    /**
     * RedisCachePut注解的实现
     * @param joinPoint 切点
     * @param redisCacheEvict
     */
    @AfterReturning(value = "@annotation(redisCacheEvict)")
    public void cacheRemove(JoinPoint joinPoint, RedisCacheEvict redisCacheEvict) {
        Object[] keyArgs = getKeyArgs(joinPoint.getArgs(), redisCacheEvict.key_args());
        String key = PlaceholderResolver.resolveByObjects(redisCacheEvict.value(), keyArgs);

        if (key != null) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 注解复合操作，可以在一个切点上绑定多个方法
     * @param joinPoint
     * @param redisCacheOption
     * @return
     * @throws Throwable
     */
    @Around("@annotation(redisCacheOption)")
    public Object cacheOptionAround(ProceedingJoinPoint joinPoint, RedisCacheOption redisCacheOption) throws Throwable {
        RedisCachePut[] redisCachePuts = redisCacheOption.cachePut();
        RedisCacheEvict[] redisCacheEvicts = redisCacheOption.cacheEvict();
        Object result = joinPoint.proceed();

        for (RedisCachePut redisCachePut : redisCachePuts) {
            cachePut(joinPoint, redisCachePut, result);
        }

        for (RedisCacheEvict redisCacheEvict : redisCacheEvicts) {
            cacheRemove(joinPoint, redisCacheEvict);
        }
        return result;
    }

    /**
     * 根据绑定在注解的key_args，返回需要格式化的顺序
     * @param args 切点的参数
     * @param keyArgs 注解绑定的key_args值
     * @return
     */
    private Object[] getKeyArgs(Object[] args, int[] keyArgs) {
        Object[] redisKeyArgs;
        int len = keyArgs.length;
        if (len == 0) {
            return args;
        }

        redisKeyArgs = new Object[len];
        for (int i = 0; i < keyArgs.length; i++) {
            redisKeyArgs[i] = args[keyArgs[i]];
        }
        return redisKeyArgs;
    }

    /**
     * 从Redis中获取对象
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    private <T> T get(String key, Class<T> clazz) {
        return (T) redisTemplate.opsForValue().get(key);
    }
}
