# 仓库管理系统 - 技术亮点总结

## 一、技术栈

- **后端框架：** Spring Boot 3.5.7 + Groovy + Java 17
- **ORM：** MyBatis-Plus 3.5.14
- **缓存：** Redis（查询缓存、Token存储）
- **消息队列：** RabbitMQ（异步日志、邮件发送）
- **安全框架：** Spring Security + JWT 4.4.0
- **文档：** Swagger 2.7.0

---

## 二、核心技术亮点

### 1. JWT 双 Token 认证机制
- **AccessToken（30分钟）** + **RefreshToken（7天）** 双Token设计
- Token存储在Redis中，支持主动失效和过期管理
- 自定义 `JwtAuthenticationFilter` 实现无状态认证
- 前端自动刷新Token机制，提升用户体验

### 2. Redis 智能缓存策略
- **缓存续期机制**：缓存命中时自动更新过期时间，提高热点数据命中率
- **权限感知缓存**：商品缓存Key包含用户管理的仓库ID，确保数据安全隔离
- **分层过期时间**：单个对象30分钟、列表10-15分钟、验证码2分钟
- **自动缓存清除**：数据更新/删除时使用通配符清除所有相关缓存变体

### 3. RabbitMQ 异步解耦
- **操作日志异步化**：所有商品操作通过消息队列异步记录，不阻塞主业务
- **邮件发送异步化**：验证码邮件异步发送，接口响应时间从2-5秒降至<100ms
- **Exchange类型应用**：Direct Exchange（精确路由日志）、Fanout Exchange（广播邮件）
- **消息可靠性**：自动重试机制（最多3次，指数退避），完善的异常处理

### 4. Spring Security + AOP 权限控制
- **JWT认证过滤器**：自定义 `JwtAuthenticationFilter` 集成Spring Security
- **注解式权限控制**：`@RequireAdmin`、`@RequireOperator` 实现方法级权限验证
- **三级角色体系**：普通用户（查询）、操作员（商品操作）、管理员（系统管理）
- **数据级权限**：用户只能操作自己管理的仓库数据，后端强制验证

### 5. AOP 切面编程
- **自动字段填充**：创建时间、更新时间、操作人等字段自动填充
- **权限切面验证**：`SecurityAspect` 在方法执行前进行权限检查
- **统一异常处理**：全局异常处理器统一处理业务异常

### 6. Excel 导出功能
- 使用 EasyExcel 实现高性能Excel导出
- 支持按仓库ID导出商品列表
- 自动处理中文文件名编码

---

## 三、性能优化

### 缓存优化
- 查询结果缓存，减少数据库压力
- 缓存续期机制提高热点数据命中率
- 精确的缓存清除策略，避免脏数据

### 异步处理
- 操作日志异步记录，不影响主业务性能
- 邮件发送异步化，接口响应时间优化90%+
- 消息队列解耦，提升系统可扩展性

### 数据库优化
- **MyBatis-Plus 分页查询**：使用Page对象实现分页，支持自定义分页插件
- **批量操作支持**：`saveBatch()` 批量插入，提升性能
- **合理的索引设计**：主键索引、外键索引，优化查询性能
- **多表关联设计**：一对多、多对多关系，体现业务关联
- **MyBatis配置优化**：
  - 下划线转驼峰：`map-underscore-to-camel-case: true`
  - Mapper XML扫描路径配置
  - 实体类别名包配置
  - 时区问题解决：`serverTimezone=Asia/Shanghai`

---

## 四、安全特性

- **密码加密**：BCrypt 加密存储
- **参数验证**：Jakarta Validation + 自定义验证器（邮箱、手机号）
- **CORS配置**：支持前后端分离
- **全局异常处理**：统一的错误响应格式
- **数据隔离**：用户只能访问自己管理的仓库数据

### 自定义校验注解
- **@Phone 注解**：自定义手机号格式校验（11位数字，以13/14/15/17/18/19开头）
- **@CustomEmail 注解**：自定义邮箱格式校验
- 支持字段和方法参数校验
- 可自定义校验失败提示信息
- 实现 `ConstraintValidator` 接口，集成 Jakarta Validation

### CORS 跨域配置
- **全局CORS配置**：`CorsConfigurationSource` Bean配置
- **允许的请求方法**：GET、POST、PUT、DELETE、OPTIONS
- **允许的请求头**：Content-Type、Authorization等（支持通配符）
- **支持携带凭证**：`allowCredentials(true)`，支持Cookie传递
- **预检请求缓存**：`maxAge(3600)`，减少OPTIONS请求

### 全局异常处理
- **@RestControllerAdvice**：统一异常处理切面
- **三类异常处理**：
  - 自定义业务异常（BaseException）：返回对应错误码和信息
  - 请求参数校验异常（MethodArgumentNotValidException）：@Valid @RequestBody校验失败
  - 方法参数校验异常（ConstraintViolationException）：@Valid @RequestParam等校验失败
- **统一响应格式**：Result类封装（code、msg、data），提供静态方法（success()、fail()）
- **HTTP状态码规范**：业务异常401，参数校验异常400

---

## 五、代码质量

- **统一响应格式**：Result 统一封装（code、msg、data、timestamp）
- **DTO/VO分离**：清晰的数据传输对象设计，实现前后端数据解耦
- **常量统一管理**：Redis Key、MQ Routing Key、验证消息等统一管理
- **Swagger文档**：完整的API文档（Knife4j集成），支持在线测试
- **代码规范**：统一的编码风格和异常处理模式

### 分层架构设计
- **Entity层**：实体类与数据库表一一对应，使用MyBatis-Plus注解
- **Mapper层**：接口与XML分离，支持复杂SQL和动态SQL
- **Service层**：接口定义与实现分离，业务逻辑封装
- **Controller层**：RESTful风格API设计，统一响应格式

### 复杂数据持久化
- **多表关联查询**：一对一、一对多关系处理（用户-仓库多对多）
- **动态条件查询**：多参数组合筛选，使用MyBatis-Plus的Wrapper动态构建
- **分页查询优化**：MyBatis-Plus分页插件，性能与用户体验平衡
- **复杂结果集映射**：VO对象映射，包含关联数据（如GoodsInWarehouseVO）

### RESTful API设计
- **标准规范**：遵循RESTful设计规范（GET查询、POST创建、PUT更新、DELETE删除）
- **统一响应格式**：Result类统一封装，包含code、msg、data
- **状态码规范**：合理使用HTTP状态码（200成功、400参数错误、401认证失败）
- **接口文档**：Swagger自动生成，支持在线测试和参数说明

---

