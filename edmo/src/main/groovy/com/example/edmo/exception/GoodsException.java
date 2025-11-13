package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class GoodsException extends BaseException {
    private final int code;

    public GoodsException(int code, String message) {
        super(message);
        this.code = code;
    }

    public GoodsException(String message) {
        super(message);
        this.code = 401;
    }
}
