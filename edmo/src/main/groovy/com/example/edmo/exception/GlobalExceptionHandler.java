package com.example.edmo.exception;

import com.example.edmo.controller.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result> handleRuntimeException(BaseException e) {
        log.warn("业务异常: {}", e.getMessage());
        int httpStatus = 401;
        return ResponseEntity.status(httpStatus).body(Result.fail(e.getCode(), e.getMessage()));
    }

    //todo 处理 @Valid @RequestBody 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = "参数校验失败";
        if (e.getBindingResult().hasFieldErrors()) {
            errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        log.warn("请求体参数校验异常: {}", errorMessage);
        return ResponseEntity.status(400).body(Result.fail(400, errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = "参数校验失败";
        if (e.getConstraintViolations() != null && e.getConstraintViolations().iterator().hasNext()) {
            errorMessage = e.getConstraintViolations().iterator().next().getMessage();
        }
        log.warn("方法参数校验异常: {}", errorMessage);
        return ResponseEntity.status(400).body(Result.fail(400, errorMessage));
    }
}
