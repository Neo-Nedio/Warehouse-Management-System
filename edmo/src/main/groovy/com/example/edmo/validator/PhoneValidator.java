package com.example.edmo.validator;

import com.example.edmo.annotation.Phone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[345789]\\d{9}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}