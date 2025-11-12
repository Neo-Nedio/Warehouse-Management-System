package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class goodsException extends BaseException {
    private final int code;

    public goodsException(int code, String message) {
        super(message);
        this.code = code;
    }

    public goodsException(String message) {
        super(message);
        this.code = 401;
    }
}
