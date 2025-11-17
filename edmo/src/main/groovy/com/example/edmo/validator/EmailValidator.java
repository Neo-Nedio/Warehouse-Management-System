package com.example.edmo.validator;

import com.example.edmo.annotation.CustomEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<CustomEmail, String> {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
