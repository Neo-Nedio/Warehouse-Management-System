package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class JwtException extends BaseException {
    private final int code;

    public JwtException(int code, String message) {
        super(message);
        this.code = code;
    }

    public JwtException(String message) {
        super(message);
        this.code = 401;
    }
}
