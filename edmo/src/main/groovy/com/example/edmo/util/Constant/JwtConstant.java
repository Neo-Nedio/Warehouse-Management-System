package com.example.edmo.util.Constant;

public class JwtConstant {
    public static String NULL_TOKEN ="没有登录";
    public static String FALSE_TOKEN ="token无效";
    public static String TOKEN_INVALID_OR_EXPIRED = "Token无效或已过期";
    public static String INVALID_USER_INFO = "用户信息无效";
    public static String NEED_ADMINISTRATOR="需要管理员权限";
    public static String NEED_ROLE="需要操作权限";

    public static final Integer ACCESS_TOKEN_EXPIRE = 30 * 60 * 1000;
    public static final Integer REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000;
    public static final String SECRET_KEY = "abcdefghijklmn";
}
