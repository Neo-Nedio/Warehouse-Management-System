package com.example.edmo.security;

import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.JwtConstant;
import com.example.edmo.exception.BaseException;
import com.example.edmo.exception.JwtException;
import com.example.edmo.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class SecurityAspect {

    @Before("@annotation(com.example.edmo.annotation.RequireAdmin)")
    public void checkAdmin(JoinPoint joinPoint) {
        if (!SecurityUtil.isAdmin()) {
            //!SecurityUtil.isAdmin()只是知道是否可以访问，在这里在判断是没登录还是没权限
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                throw new BaseException(CodeConstant.token, JwtConstant.NULL_TOKEN);
            }
            log.warn("用户{}，{}没有权限访问",currentUser.getName(), currentUser.getEmail());
            throw new JwtException(CodeConstant.role, JwtConstant.NEED_ADMINISTRATOR);
        }
    }

    @Before("@annotation(com.example.edmo.annotation.RequireOperator)")
    public void checkOperator(JoinPoint joinPoint) {
        if (!SecurityUtil.hasOperatorRole()) {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                throw new BaseException(CodeConstant.token, JwtConstant.NULL_TOKEN);
            }
            log.warn("用户{}，{}没有权限访问",currentUser.getName(), currentUser.getEmail());
            throw new JwtException(CodeConstant.role, JwtConstant.NEED_ROLE);
        }
    }
}

