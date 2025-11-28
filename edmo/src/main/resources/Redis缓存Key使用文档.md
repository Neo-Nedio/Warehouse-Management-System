# Redis 缓存 Key 使用文档

本文档详细说明了系统中所有 Redis Key 的格式、用途、过期时间和操作方式。

redis问题排查：

检查并重启VMware网络服务（在Windows主机上）
   在Windows主机上：

按 Win + R，输入 services.msc

找到以下服务并重启：

VMware NAT Service

VMware DHCP Service

VMware Authorization Service

或者直接重启所有VMware相关服务
## 目录

- [验证码相关 Key](#验证码相关-key)
- [用户缓存相关 Key](#用户缓存相关-key)
- [商品缓存相关 Key](#商品缓存相关-key)
- [仓库缓存相关 Key](#仓库缓存相关-key)
- [Key 设计原则](#key-设计原则)
- [缓存清除策略](#缓存清除策略)

---

## 验证码相关 Key

### 1. 登录验证码

**Key 格式：** `user:code:{email}`

**完整示例：** `user:code:user@example.com`

**用途：** 存储用户登录验证码

**数据类型：** String（验证码数字，如 "123456"）

**过期时间：** 2 分钟

**操作位置：**
- **设置：** `UserController.createCode()` - 发送验证码时存储
- **读取：** `UserController.loginByCode()` - 验证码登录时验证
- **读取：** `UserController.updatePassword()` - 更新密码时验证
- **删除：** 验证成功后立即删除（防止重复使用）

**使用场景：**
```java
// 存储验证码
stringRedisTemplate.opsForValue().set(
    RedisConstant.LOGIN_CODE_KEY + email, 
    String.valueOf(code), 
    RedisConstant.LOGIN_CODE_TTL, 
    TimeUnit.MINUTES
);

// 读取验证码
String code = stringRedisTemplate.opsForValue().get(
    RedisConstant.LOGIN_CODE_KEY + email
);

// 删除验证码（验证成功后）
stringRedisTemplate.delete(RedisConstant.LOGIN_CODE_KEY + email);
```

---

### 2. 验证码发送频率限制

**Key 格式：** `user:code:limit:{email}`

**完整示例：** `user:code:limit:user@example.com`

**用途：** 限制验证码发送频率，防止频繁发送

**数据类型：** String（时间戳，如 "1704067200000"）

**过期时间：** 1 分钟

**操作位置：**
- **设置：** `UserServiceImpl.CreateCode()` - 发送验证码后设置
- **读取：** `UserServiceImpl.CreateCode()` - 检查是否在1分钟内已发送

**使用场景：**
```java
// 检查是否已发送
String lastSendTime = stringRedisTemplate.opsForValue().get(limitKey);
if (lastSendTime != null) {
    throw new UserException(CodeConstant.user, UserConstant.FALSE_GET);
}

// 设置限制标记
stringRedisTemplate.opsForValue().set(
    limitKey, 
    String.valueOf(System.currentTimeMillis()),
    1, 
    TimeUnit.MINUTES
);
```

---

## 用户缓存相关 Key

### 3. 单个用户缓存（按ID）

**Key 格式：** `user:id:{id}`

**完整示例：** `user:id:1`

**用途：** 缓存单个用户信息

**数据类型：** String（JSON 格式的 User 对象）

**过期时间：** 30 分钟

**操作位置：**
- **设置：** `UserServiceImpl.getById()` - 查询用户时缓存
- **读取：** `UserServiceImpl.getById()` - 查询用户时优先从缓存获取
- **删除：** `UserServiceImpl.updateById()` - 更新用户时清除
- **删除：** `UserServiceImpl.removeById()` - 删除用户时清除

**使用场景：**
```java
// 从缓存获取
String cacheKey = RedisConstant.USER_KEY + "id:" + id;
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toBean(cacheValue, User.class);
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(user), 
    RedisConstant.USER_TTL, 
    TimeUnit.MINUTES
);
```

---

### 4. 单个用户缓存（按邮箱）

**Key 格式：** `user:email:{email}`

**完整示例：** `user:email:user@example.com`

**用途：** 缓存按邮箱查询的用户信息

**数据类型：** String（JSON 格式的 User 对象）

**过期时间：** 30 分钟

**操作位置：**
- **设置：** `UserServiceImpl.findUserByEmail()` - 查询用户时缓存
- **读取：** `UserServiceImpl.findUserByEmail()` - 查询用户时优先从缓存获取
- **删除：** `UserServiceImpl.updateById()` - 更新用户时清除（如果邮箱改变）
- **删除：** `UserServiceImpl.removeById()` - 删除用户时清除

**使用场景：**
```java
// 从缓存获取
String cacheKey = RedisConstant.USER_KEY + "email:" + email;
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toBean(cacheValue, User.class);
}

// 存入缓存
if (user != null) {
    stringRedisTemplate.opsForValue().set(
        cacheKey, 
        JSONUtil.toJsonStr(user), 
        RedisConstant.USER_TTL, 
        TimeUnit.MINUTES
    );
}
```

---

### 5. 用户列表缓存（按名称）

**Key 格式：** `user:list:name:{name}`

**完整示例：** `user:list:name:张三`

**用途：** 缓存按名称模糊查询的用户列表

**数据类型：** String（JSON 格式的 User 列表）

**过期时间：** 10 分钟

**操作位置：**
- **设置：** `UserServiceImpl.findUsersByNameLike(String)` - 查询用户列表时缓存
- **读取：** `UserServiceImpl.findUsersByNameLike(String)` - 查询用户列表时优先从缓存获取
- **删除：** `UserServiceImpl.save()` - 新增用户时清除所有列表缓存
- **删除：** `UserServiceImpl.updateById()` - 更新用户时清除所有列表缓存
- **删除：** `UserServiceImpl.removeById()` - 删除用户时清除所有列表缓存

**使用场景：**
```java
// 从缓存获取
String cacheKey = RedisConstant.USER_LIST_KEY + "name:" + name;
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return fillManagedWarehouseIds(JSONUtil.toList(cacheValue, User.class));
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(users), 
    RedisConstant.USER_LIST_TTL, 
    TimeUnit.MINUTES
);

// 清除所有列表缓存（使用通配符）
var keys = stringRedisTemplate.keys(RedisConstant.USER_LIST_KEY + "*");
if (!keys.isEmpty()) {
    stringRedisTemplate.delete(keys);
}
```

---

### 6. 用户列表缓存（按ID列表）

**Key 格式：** `user:list:ids:{ids}`

**完整示例：** `user:list:ids:[1, 2, 3]`

**用途：** 缓存批量查询的用户列表

**数据类型：** String（JSON 格式的 User 列表）

**过期时间：** 10 分钟

**操作位置：**
- **设置：** `UserServiceImpl.findUsersByIds()` - 批量查询用户时缓存
- **读取：** `UserServiceImpl.findUsersByIds()` - 批量查询用户时优先从缓存获取
- **删除：** 同用户列表缓存（按名称）的清除策略

**使用场景：**
```java
// 构建缓存 key（使用 List.toString()）
String cacheKey = RedisConstant.USER_LIST_KEY + "ids:" + ids.toString();

// 从缓存获取
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return fillManagedWarehouseIds(JSONUtil.toList(cacheValue, User.class));
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(users), 
    RedisConstant.USER_LIST_TTL, 
    TimeUnit.MINUTES
);
```

---

## 商品缓存相关 Key

### 7. 单个商品缓存

**Key 格式：** `goods:id:{id}:warehouses:{warehouseIds}`

**完整示例：** `goods:id:1:warehouses:[1, 2, 3]`

**用途：** 缓存单个商品信息（包含权限信息）

**数据类型：** String（JSON 格式的 Goods 对象）

**过期时间：** 30 分钟

**说明：** 
- 同一商品 ID 可能对应不同的 `managedWarehouseIds`，因此会有多个缓存 key
- 例如：`goods:id:1:warehouses:[1,2]` 和 `goods:id:1:warehouses:[1,2,3]` 是两个不同的缓存
- 这是因为不同用户管理的仓库不同，查询结果可能不同

**操作位置：**
- **设置：** `GoodsServiceImpl.findGoodsById()` - 查询商品时缓存
- **读取：** `GoodsServiceImpl.findGoodsById()` - 查询商品时优先从缓存获取
- **删除：** `GoodsServiceImpl.save()` - 新增商品时清除相关缓存（通过通配符）
- **删除：** `GoodsServiceImpl.updateById()` - 更新商品时清除所有该商品ID的缓存变体
- **删除：** `GoodsServiceImpl.removeById()` - 删除商品时清除所有该商品ID的缓存变体
- **删除：** `GoodsServiceImpl.updateGoodsInWarehouse()` - 更新商品仓库时清除
- **删除：** `GoodsServiceImpl.loginDeleteGoodsById()` - 逻辑删除商品时清除

**使用场景：**
```java
// 构建缓存 key（包含 warehouseIds）
String cacheKey = RedisConstant.GOODS_KEY + "id:" + id 
    + ":warehouses:" + managedWarehouseIds.toString();

// 从缓存获取
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toBean(cacheValue, Goods.class);
}

// 存入缓存
if (goods != null) {
    stringRedisTemplate.opsForValue().set(
        cacheKey, 
        JSONUtil.toJsonStr(goods), 
        RedisConstant.GOODS_TTL, 
        TimeUnit.MINUTES
    );
}

// 清除所有该商品ID的缓存变体（使用通配符）
var keys = stringRedisTemplate.keys(
    RedisConstant.GOODS_KEY + "id:" + goodsId + ":*"
);
if (!keys.isEmpty()) {
    stringRedisTemplate.delete(keys);
}
```

---

### 8. 仓库商品缓存（按仓库ID）

**Key 格式：** `goods:warehouse:{warehouseId}:warehouses:{managedWarehouseIds}`

**完整示例：** `goods:warehouse:1:warehouses:[1, 2, 3]`

**用途：** 缓存某个仓库的商品列表（包含仓库信息和商品列表）

**数据类型：** String（JSON 格式的 GoodsInWarehouseVO 对象）

**过期时间：** 15 分钟

**说明：**
- 包含仓库基本信息和该仓库下的商品列表
- 同样需要考虑用户权限（managedWarehouseIds）

**操作位置：**
- **设置：** `GoodsServiceImpl.findGoodsByWarehouseId()` - 查询仓库商品时缓存
- **读取：** `GoodsServiceImpl.findGoodsByWarehouseId()` - 查询仓库商品时优先从缓存获取
- **删除：** `GoodsServiceImpl.save()` - 新增商品时清除所有仓库商品缓存
- **删除：** `GoodsServiceImpl.updateById()` - 更新商品时清除所有仓库商品缓存
- **删除：** `GoodsServiceImpl.removeById()` - 删除商品时清除所有仓库商品缓存
- **删除：** `GoodsServiceImpl.updateGoodsInWarehouse()` - 更新商品仓库时清除
- **删除：** `GoodsServiceImpl.loginDeleteGoodsById()` - 逻辑删除商品时清除

---

### 8.1. 商品列表缓存（按名称）

**Key 格式：** `goods:warehouse:name:{name}:warehouses:{managedWarehouseIds}`

**完整示例：** `goods:warehouse:name:商品A:warehouses:[1, 2, 3]`

**用途：** 缓存按名称模糊查询的商品列表

**数据类型：** String（JSON 格式的 Goods 列表）

**过期时间：** 15 分钟

**操作位置：**
- **设置：** `GoodsServiceImpl.findGoodsByNameLike(String, ...)` - 按名称查询商品时缓存
- **读取：** `GoodsServiceImpl.findGoodsByNameLike(String, ...)` - 按名称查询商品时优先从缓存获取
- **删除：** 同仓库商品缓存的清除策略（通过通配符 `goods:warehouse:*`）

---

### 8.2. 所有商品缓存（按管理的仓库）

**Key 格式：** `goods:warehouse:all:warehouses:{managedWarehouseIds}`

**完整示例：** `goods:warehouse:all:warehouses:[1, 2, 3]`

**用途：** 缓存用户管理的所有仓库中的商品列表

**数据类型：** String（JSON 格式的 GoodsInWarehouseVO 列表）

**过期时间：** 15 分钟

**操作位置：**
- **设置：** `GoodsServiceImpl.findGoodsAllByManagedWarehouseIds()` - 查询所有商品时缓存
- **读取：** `GoodsServiceImpl.findGoodsAllByManagedWarehouseIds()` - 查询所有商品时优先从缓存获取
- **删除：** 同仓库商品缓存的清除策略（通过通配符 `goods:warehouse:*`）

**使用场景：**
```java
// 构建缓存 key
String cacheKey = RedisConstant.GOODS_WAREHOUSE_KEY 
    + warehouseId + ":warehouses:" + managedWarehouseIds.toString();

// 从缓存获取
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toBean(cacheValue, GoodsInWarehouseVO.class);
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(goodsInWarehouseVO), 
    RedisConstant.GOODS_WAREHOUSE_TTL, 
    TimeUnit.MINUTES
);

// 清除所有仓库商品缓存（使用通配符）
var warehouseKeys = stringRedisTemplate.keys(
    RedisConstant.GOODS_WAREHOUSE_KEY + "*"
);
if (!warehouseKeys.isEmpty()) {
    stringRedisTemplate.delete(warehouseKeys);
}
```

---

## 仓库缓存相关 Key

### 9. 单个仓库缓存（按ID）

**Key 格式：** `warehouse:id:{id}`

**完整示例：** `warehouse:id:1`

**用途：** 缓存单个仓库信息

**数据类型：** String（JSON 格式的 Warehouse 对象）

**过期时间：** 30 分钟

**操作位置：**
- **设置：** `WarehouseServiceImpl.getById()` - 查询仓库时缓存
- **读取：** `WarehouseServiceImpl.getById()` - 查询仓库时优先从缓存获取
- **删除：** `WarehouseServiceImpl.save()` - 新增仓库时清除（如果id不为null）
- **删除：** `WarehouseServiceImpl.updateById()` - 更新仓库时清除（如果id不为null）
- **删除：** `WarehouseServiceImpl.removeById()` - 删除仓库时清除

**使用场景：**
```java
// 从缓存获取
String cacheKey = RedisConstant.WAREHOUSE_KEY + "id:" + id;
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toBean(cacheValue, Warehouse.class);
}

// 存入缓存
if (warehouse != null) {
    stringRedisTemplate.opsForValue().set(
        cacheKey, 
        JSONUtil.toJsonStr(warehouse), 
        RedisConstant.WAREHOUSE_TTL, 
        TimeUnit.MINUTES
    );
}

// 清除单个仓库缓存
stringRedisTemplate.delete(RedisConstant.WAREHOUSE_KEY + "id:" + id);
```

---

### 10. 仓库列表缓存（按名称）

**Key 格式：** `warehouse:list:name:{name}`

**完整示例：** `warehouse:list:name:北京仓库`

**用途：** 缓存按名称模糊查询的仓库列表

**数据类型：** String（JSON 格式的 Warehouse 列表）

**过期时间：** 10 分钟

**操作位置：**
- **设置：** `WarehouseServiceImpl.findWarehousesByNameLike(String)` - 查询仓库列表时缓存
- **读取：** `WarehouseServiceImpl.findWarehousesByNameLike(String)` - 查询仓库列表时优先从缓存获取
- **删除：** `WarehouseServiceImpl.save()` - 新增仓库时清除所有列表缓存
- **删除：** `WarehouseServiceImpl.updateById()` - 更新仓库时清除所有列表缓存
- **删除：** `WarehouseServiceImpl.removeById()` - 删除仓库时清除所有列表缓存

**使用场景：**
```java
// 从缓存获取
String cacheKey = RedisConstant.WAREHOUSE_LIST_KEY + "name:" + name;
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toList(cacheValue, Warehouse.class);
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(warehouses), 
    RedisConstant.WAREHOUSE_LIST_TTL, 
    TimeUnit.MINUTES
);
```

---

### 11. 仓库列表缓存（按ID列表）

**Key 格式：** `warehouse:list:ids:{ids}`

**完整示例：** `warehouse:list:ids:[1, 2, 3]`

**用途：** 缓存批量查询的仓库列表

**数据类型：** String（JSON 格式的 Warehouse 列表）

**过期时间：** 10 分钟

**操作位置：**
- **设置：** `WarehouseServiceImpl.findWarehousesById()` - 批量查询仓库时缓存
- **读取：** `WarehouseServiceImpl.findWarehousesById()` - 批量查询仓库时优先从缓存获取
- **删除：** `WarehouseServiceImpl.save()` - 新增仓库时清除所有列表缓存
- **删除：** `WarehouseServiceImpl.updateById()` - 更新仓库时清除所有列表缓存
- **删除：** `WarehouseServiceImpl.removeById()` - 删除仓库时清除所有列表缓存

**使用场景：**
```java
// 构建缓存 key
String cacheKey = RedisConstant.WAREHOUSE_LIST_KEY + "ids:" + ids.toString();

// 从缓存获取
String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
if (cacheValue != null) {
    return JSONUtil.toList(cacheValue, Warehouse.class);
}

// 存入缓存
stringRedisTemplate.opsForValue().set(
    cacheKey, 
    JSONUtil.toJsonStr(warehouses), 
    RedisConstant.WAREHOUSE_LIST_TTL, 
    TimeUnit.MINUTES
);
```

---

## Key 设计原则

### 1. 命名规范

- **格式：** `{模块}:{类型}:{参数}`
- **分隔符：** 使用冒号 `:` 分隔层级
- **示例：** `user:id:1`、`goods:warehouse:1:warehouses:[1,2,3]`

### 2. 唯一性保证

- Key 必须包含所有影响查询结果的参数
- 例如商品缓存包含 `warehouseIds`，因为不同用户管理的仓库不同

### 3. 可读性

- 使用有意义的标识符：`id:`、`email:`、`name:`、`warehouses:` 等
- 便于调试和维护

### 4. 通配符使用

- 清除缓存时使用 `*` 匹配多个 key
- 例如：`goods:id:1:*` 匹配所有以 `goods:id:1:` 开头的 key

---

## 缓存清除策略

### 清除时机

#### 1. 数据更新时
- **用户更新：** 清除该用户的单个缓存（按ID和按邮箱，如果email不为null）和所有列表缓存
- **商品更新：** 清除该商品的所有缓存变体（因为不同用户管理的仓库不同）和所有仓库商品缓存（包括按名称、按仓库ID、所有商品）
- **仓库更新：** 清除该仓库的单个缓存（如果id不为null）和所有列表缓存

#### 2. 数据删除时
- **用户删除：** 清除该用户的单个缓存（按ID和按邮箱）和所有列表缓存
- **商品删除：** 清除该商品的所有缓存变体和所有仓库商品缓存（包括按名称、按仓库ID、所有商品）
- **仓库删除：** 清除该仓库的单个缓存和所有列表缓存

#### 3. 数据新增时
- **用户新增：** 清除所有用户列表缓存（因为列表查询结果会变化）
- **商品新增：** 清除所有仓库商品缓存（包括按名称、按仓库ID、所有商品，因为新商品会出现在这些查询结果中）
- **仓库新增：** 清除该仓库的单个缓存（如果id不为null）和所有列表缓存

### 清除方式

#### 精确删除
```java
// 删除单个 key
stringRedisTemplate.delete(RedisConstant.USER_KEY + "id:" + id);
```

#### 通配符删除
```java
// 删除匹配模式的所有 key
var keys = stringRedisTemplate.keys(RedisConstant.USER_LIST_KEY + "*");
if (!keys.isEmpty()) {
    stringRedisTemplate.delete(keys);
}
```

---

## 注意事项

### 1. 缓存数据格式

- 所有对象缓存使用 JSON 格式存储
- 使用 Hutool 的 `JSONUtil` 进行序列化和反序列化
- 验证码等简单数据直接存储字符串

### 2. 过期时间设置

- **验证码：** 2 分钟（安全性考虑）
- **频率限制：** 1 分钟（防止频繁请求）
- **单个对象：** 30 分钟（用户、商品）
- **列表数据：** 10-15 分钟（列表查询结果）

### 3. 缓存穿透保护

- 查询时先检查缓存，未命中再查询数据库
- 查询结果为空时不缓存（避免缓存空值）

### 4. 数据一致性

- 更新/删除操作时立即清除相关缓存
- 使用通配符确保清除所有相关缓存变体

### 5. 权限相关缓存

- 商品和仓库商品缓存包含 `warehouseIds` 参数
- 确保不同权限用户看到正确的数据
- 清除时需要使用通配符匹配所有权限变体

---

## 总结

本系统共使用 **11 种 Redis Key 模式**：

1. **验证码相关（2种）：** 登录验证码、发送频率限制
2. **用户缓存（4种）：** 按ID、按邮箱、按名称列表、按ID列表
3. **商品缓存（4种）：** 单个商品、按仓库ID查询、按名称查询、所有商品查询
4. **仓库缓存（3种）：** 按ID、按名称列表、按ID列表

所有缓存都实现了：
- ✅ 查询时优先从缓存获取
- ✅ 缓存未命中时查询数据库并写入缓存
- ✅ 数据更新/删除时自动清除相关缓存
- ✅ 合理的过期时间设置
- ✅ 数据一致性保证

---

**文档版本：** 1.1  
**最后更新：** 2024年  
**更新内容：** 
- 添加了单个仓库缓存（warehouse:id:{id}）的说明
- 补充了商品缓存的详细分类（按仓库ID、按名称、所有商品）
- 完善了缓存清除策略的说明
- 更新了缓存 Key 模式总数（从10种增加到11种）

