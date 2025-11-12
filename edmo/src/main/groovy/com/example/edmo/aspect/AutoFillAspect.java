package com.example.edmo.aspect;

import com.example.edmo.Constant.AspectConstant;
import com.example.edmo.Constant.CodeConstant;
import com.example.edmo.Jwt.UserContext;
import com.example.edmo.annotation.AutoFill;
import com.example.edmo.enumeration.OperationType;
import com.example.edmo.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    //execution(* com.example.edmo.controller.*.*(..)) &&
    @Pointcut(" @annotation(com.example.edmo.annotation.AutoFill)")
    public void autoFillPointCut() {
    }


    @Before("autoFillPointCut()")
    public void autoFillBefore(JoinPoint joinPoint) {
        //获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }


        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        String username = UserContext.getCurrentUser().getName();



        try{
            if(operationType.equals(OperationType.INSERT)){
                entity.getClass().getMethod("setCreateUser",String.class).invoke(entity,username);
                entity.getClass().getMethod("setCreateTime",LocalDateTime.class).invoke(entity,now);
                entity.getClass().getMethod("setUpdateTime",LocalDateTime.class).invoke(entity,now);
                entity.getClass().getMethod("setUpdateUser",String.class).invoke(entity,username);
            }else if(operationType.equals(OperationType.UPDATE)){
                entity.getClass().getMethod("setUpdateTime",LocalDateTime.class).invoke(entity,now);
                entity.getClass().getMethod("setUpdateUser",String.class).invoke(entity,username);
            }
        }catch (Exception e){
            throw new BaseException(CodeConstant.system, AspectConstant.FALSE_AOP);
        }
    }
}
