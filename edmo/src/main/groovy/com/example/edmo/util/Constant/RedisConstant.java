package com.example.edmo.util.Constant;

public class RedisConstant {
    public static final String LOGIN_CODE_KEY = "user:code:";
    public static final long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_CODE_LIMIT_KEY = "user:code:limit:";  // 1分钟内不能重复发送
    public static final long LOGIN_CODE_LIMIT_TTL = 1L;  // 1分钟
}
