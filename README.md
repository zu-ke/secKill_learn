# 秒杀/高并发解决方案及落地实现

## 技术栈
SpringBoot, MySQL, Redis, RabbitMQ, MyBatis-Plus, Maven, Linux, JMeter

## 开发流程

### 1. 用户密码加密加盐
- MD5(明文 + 公共盐) --> midPassword
- MD5(midPassword + 私盐) --> resultPassword
- 每个用户有独立私盐，用户登录时，后端接收 `midPassword`，再用私盐加密后与数据库对比。
- 登录成功后，生成 UUID 混合的 `userTicket` 存入 Redis，通过 Cookie 返回前端。
- 使用 `UserArgumentResolver.java` 将形参中的 `userTicket` 换成 `user` 对象，减少每次从 Redis 获取用户对象的操作。

### 2. 响应消息枚举
- 采用枚举定义响应消息，统一管理。

### 3. 自定义注解校验手机号
- 使用 `IsMobile.java` 注解校验手机号。

### 4. 全局异常处理
- 实现全局异常和异常处理器。

### 5. JMeter 压测
- 配置和使用 JMeter 进行性能测试。

### 6. Redis 缓存
- 缓存页面和数据，可设置失效时间或在修改接口处更新，防止数据不一致。

### 7. 解决复购和超卖
- 使用 MySQL 默认事务隔离级别（REPEATABLE-READ）防止重复购买和超卖。
- 在高并发场景下，使用 Redis 预减库存，减少对数据库的压力。

### 8. RabbitMQ 配置和使用
- 安装和配置 RabbitMQ，集成 SpringBoot，使用 fanout、direct、topic 和 headers 交换机。

### 9. Redis 预减库存
- 在 `SecKillController` 实现 `InitializingBean`，重写 `afterPropertiesSet()` 方法，从数据库读取秒杀商品库存，存入 Redis，实现预减库存。

### 10. 内存标记优化高并发
- 在 `SecKillController` 中定义 `ConcurrentHashMap<Long, Boolean>`，标记商品库存状态，优化高并发处理。

### 11. RabbitMQ 消息队列异步操作
- 创建队列和 topic 交换机，优化用户体验，减少高并发情况下的线程堆积。

### 12. 秒杀接口地址隐藏
- 动态生成秒杀接口地址，防止脚本攻击。用户登录后生成 `path`，存储在 Redis，秒杀前校验 `path`。

### 13. 验证码校验
- 集成开源验证码项目 `happyCaptcha`，在秒杀前校验验证码，防止机器秒杀。

### 14. 秒杀接口限流防刷
- 自定义拦截器 `AccessLimit.java` 和 `AccessLimitInterceptor.java`，通过注解限制接口访问频率和次数，防止刷接口。
- 使用 `ThreadLocal` 存储用户对象，简化用户登录状态的获取。

### 15. Redis 分布式锁
- 使用 Redis 分布式锁防止超卖，配合 Lua 脚本实现复杂业务场景下的锁机制。
