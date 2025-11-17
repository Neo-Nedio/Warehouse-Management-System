package com.example.edmo.annotation;

import com.example.edmo.validator.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomEmail {
    String message() default "邮箱格式不正确";

    //存在但不用
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
