package com.example.edmo.filter;

import com.example.edmo.Jwt.JwtUtil;
import com.example.edmo.Jwt.UserContext;
import com.example.edmo.entity.User;
import com.example.edmo.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SimpleJwtInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 先清理线程数据
        UserContext.clear();

        // 放行登录接口
        if (request.getRequestURI().contains("/user/login")) {
            return true;
        }

        // 获取token
        String token = request.getHeader("token");
        if (token == null ) {
            // 设置字符编码 BEFORE 写入内容
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }


        // 验证token
        if (!JwtUtil.verifyToken(token)) {
            // 设置字符编码 BEFORE 写入内容
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效\"}");
            return false;
        }

        // 获取用户信息并存储
        Integer userId = JwtUtil.getUserId(token);
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserContext.setCurrentUser(user);
                return true;
            }
        }

        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"用户不存在\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        // 清理ThreadLocal
        UserContext.clear();
    }
}
