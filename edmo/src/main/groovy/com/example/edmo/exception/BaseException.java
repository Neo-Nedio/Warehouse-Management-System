package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final int code;

    public BaseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        super(message);
        this.code = 401;
    }

}
