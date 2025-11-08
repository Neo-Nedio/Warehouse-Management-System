package com.example.edmo.filter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class SendError {
    public static void Error(HttpServletResponse response, int code, String message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(code);
            response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
        } catch (IOException e) {
            // 内部处理异常，不让调用方操心
            try {
                response.sendError(code, message);  // 备选方案
            } catch (IOException ignored) {
                response.setStatus(code);  // 最后手段
            }
        }
    }
}
