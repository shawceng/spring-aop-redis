## Redis AOP实现
采用AOP抽取出redis缓存操作，解耦代码，代码侵入性低
### 思路
通过 Spring 的 AOP 技术，拦截从数据库查询的方法，在从数据库获取结果之前，先从 redis 获取，如果 redis 命中，则直接返回；否则就继续执行从数据库获取的方法，将返回值缓存到 reids 并返回
组件包含:
- 缓存存放`@RedisCacheable`，
- 缓存更新`@RedisCachePut`，
- 缓存删除`@RedisCacheEvict` ,
- 复合操作`@RedisCacheOption`
### 使用方法
示例对象与方法
```java
// domain类
class User {
    int id;
    String name;
}
// service类
class UserService {
    Object hello(Object ...values) {}
}
```
#### RedisCacheable
##### value 必须
使用`{field}`作为占位符，其中`field`为对应参数对象的属性, 原始类型可以不添加`field`。比如`test:{name}`
#### key_args 可选
用于调整切点方法参数传入切面的顺序，比如`Object hello(Object obj1, Object obj2, Object obj3)， 该切点传入的key_args为{1, 2, 0},则格式化的顺序为`obj2 obj3 obj0`
#### action 可选
是否穿透，可选`RedisCacheable.REDIS_FIRST`与`RedisCacheable.REDIS_STAB`，后者为穿透
#### ttl 可选
传入该键值在redis中的过期时间，单位为秒
#### 示例
```java
class UserService {
    @RedisCacheable("USER:{id}") // key: USER:123
    Object hello(User user) {}

    @RedisCacheable(value = "USER2:{}-{}{id}", key_args={2, 1, 0}) // key: USER2:niuni-123-123
    Object hello(User user, Long id, String name) {} 
}
```
#### RedisCacheOption
复合操作
#### 示例
```java
class UserService {
    @RedisCacheOption(
                cachePut = {
                        @RedisCachePut(value = "USER:{}", key_args = {0, })
                },
                cacheEvict = {
                        @RedisCacheEvict(value = "USER:{}", key_args = {0, })
                        @RedisCacheEvict(value = "ALL_USER")
                }
        )
    Object hello(User user, Long id, String name) {} 
}
```
