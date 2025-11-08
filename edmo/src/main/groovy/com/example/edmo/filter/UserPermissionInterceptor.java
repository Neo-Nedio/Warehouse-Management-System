package com.example.edmo.filter;


import com.example.edmo.Jwt.UserContext;
import com.example.edmo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class UserPermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 从线程中获取用户信息
        User currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            SendError.Error(response, 401, "用户未登录");
            return false;
        }

        // 查询接口：所有权限都可以访问（包括POST查询）
        if (isQueryOperation(uri, method)) {
            return true;
        }
        // 其他操作：需要管理员权限
        if (currentUser.getRoleId() != 3) { // 假设3是管理员
            SendError.Error(response, 403, "需要管理员权限");
            return false;
        }

        return true;
    }

    /**
     * 判断是否为查询操作
     */
    private boolean isQueryOperation(String uri, String method) {
        // 所有GET请求都是查询
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }
        // 特定的POST查询接口
        if ("POST".equalsIgnoreCase(method)) {
            return uri.endsWith("/listPage") ||
                    uri.endsWith("/findByNameLike");
        }

        return false;
    }

}

