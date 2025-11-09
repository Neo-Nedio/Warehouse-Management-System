package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class WarehouseException extends BaseException {
    private final int code;

    public WarehouseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WarehouseException(String message) {
        super(message);
        this.code = 401;
    }
}
