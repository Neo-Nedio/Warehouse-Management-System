package com.example.edmo.exception;

import lombok.Getter;

@Getter
public class OperationLogException extends RuntimeException {
    private final int code;

    public OperationLogException(int code, String message) {
        super(message);
        this.code = code;
    }

    public OperationLogException(String message) {
        super(message);
        this.code = 401;
    }
}
