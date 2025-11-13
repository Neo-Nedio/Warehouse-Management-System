package com.example.edmo.util.interceptor;


import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.util.Jwt.UserContext;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.exception.BaseException;
import com.example.edmo.exception.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class UserPermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            // 从线程中获取用户信息
            User currentUser = UserContext.getCurrentUser();

            if (isQueryOperation(uri, method)) {
                return true;
            }
            if (currentUser.getRoleId() != 3) { // 假设3是管理员
                throw new JwtException(CodeConstant.role, JwtConstant.NEED_ADMINISTRATOR);
            }
        }catch (Exception e)
        {
            throw new BaseException(CodeConstant.role,e.getMessage());
        }

        return true;
    }

    /**
     * 判断是否为查询操作
     */
    private boolean isQueryOperation(String uri, String method) {

        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method)) {
            return uri.endsWith("/listPage") ||
                    uri.endsWith("/findByNameLike") ||
                    uri.endsWith("/findById");
        }

        return false;
    }

}

