package com.example.edmo.util.Constant;


public class RedisConstant {
    // 验证码相关
    public static final String LOGIN_CODE_KEY = "user:code:";
    public static final long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_CODE_LIMIT_KEY = "user:code:limit:";  // 1分钟内不能重复发送
    public static final long LOGIN_CODE_LIMIT_TTL = 1L;  // 1分钟

    // 用户缓存相关
    public static final String USER_KEY = "user:";
    public static final long USER_TTL = 30L;  // 30分钟
    public static final String USER_LIST_KEY = "user:list:";
    public static final long USER_LIST_TTL = 10L;  // 10分钟

    // 商品缓存相关
    public static final String GOODS_KEY = "goods:";
    public static final long GOODS_TTL = 30L;  // 30分钟
    public static final String GOODS_WAREHOUSE_KEY = "goods:warehouse:";
    public static final long GOODS_WAREHOUSE_TTL = 15L;  // 15分钟

    // 仓库缓存相关
    public static final String WAREHOUSE_KEY = "warehouse:";
    public static final long WAREHOUSE_TTL = 30L;  // 30分钟
    public static final String WAREHOUSE_LIST_KEY = "warehouse:list:";
    public static final long WAREHOUSE_LIST_TTL = 10L;  // 10分钟

    // Token会话相关
    public static final String TOKEN_KEY = "token:";
    public static final long TOKEN_TTL = 30L;  // 30分钟
    public static final String REFRESH_TOKEN_KEY = "refresh_token:";
    public static final long REFRESH_TOKEN_TTL = 7 * 24 * 60L;  // 7天
}
