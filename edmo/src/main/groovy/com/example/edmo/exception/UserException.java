package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class UserException extends BaseException{
    private final int code;

    public UserException(int code, String message) {
        super(message);
        this.code = code;
    }

    public UserException(String message) {
        super(message);
        this.code = 401;
    }
}
