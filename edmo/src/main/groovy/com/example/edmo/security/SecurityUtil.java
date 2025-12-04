package com.example.edmo.security;

import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Jwt.UserContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SecurityUtil {
    public static User getCurrentUser() {
        // 优先从UserContext获取（保持向后兼容）
        User user = UserContext.getCurrentUser();
        if (user != null) {
            return user;
        }

        // 从Spring Security上下文获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        return null;
    }

    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRoleId() == 3;
    }

    public static boolean hasOperatorRole() {
        User user = getCurrentUser();
        return user != null && user.getRoleId() >= 2;
    }

}





