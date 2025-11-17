package com.example.edmo.annotation;

import com.example.edmo.validator.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Phone {
    String message() default "手机号格式不正确";

    //存在但不用
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
