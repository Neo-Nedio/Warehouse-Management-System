package com.example.edmo.filter;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class SendError {
    public  static void Error(HttpServletResponse response, int code, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
    }
}
