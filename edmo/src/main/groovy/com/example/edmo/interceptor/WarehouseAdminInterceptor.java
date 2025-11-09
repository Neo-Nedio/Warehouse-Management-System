package com.example.edmo.interceptor;

import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Constant.JwtConstant;
import com.example.edmo.Jwt.UserContext;
import com.example.edmo.exception.BaseException;
import com.example.edmo.exception.JwtException;
import com.example.edmo.pojo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class WarehouseAdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        try {
            // 从线程中获取用户信息
            User currentUser = UserContext.getCurrentUser();

            if (currentUser.getRoleId() != 3) { // 假设3是管理员
                throw new JwtException(CodeConstant.role, JwtConstant.NEED_ADMINISTRATOR);
            }
        }catch (Exception e)
        {
            throw new BaseException(CodeConstant.role,e.getMessage());
        }

        return true;
    }
}
