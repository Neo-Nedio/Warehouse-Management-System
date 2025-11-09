package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class BusinessException extends BaseException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 401;
    }
}
