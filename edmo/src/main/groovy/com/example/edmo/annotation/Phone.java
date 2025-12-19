package com.example.edmo.annotation;

import com.example.edmo.validator.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented  // 生成JavaDoc时包含此注解
@Constraint(validatedBy = PhoneValidator.class)  //  指定校验器类
@Target({ElementType.FIELD, ElementType.PARAMETER})  //  可用于字段和方法参数
@Retention(RetentionPolicy.RUNTIME)  //  运行时保留，可通过反射获取
public @interface Phone {
    String message() default "手机号格式不正确";

    //存在但不用
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
