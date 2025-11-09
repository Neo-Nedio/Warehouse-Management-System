package com.example.edmo.controller;

import lombok.Data;

@Data
public class Result {
    int code;
    String msg;
    Object data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    private static Result result(int code, String msg, Object data) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }


   public static Result success(Object data ) {
        return  result(200,"success", data);
   }

    public static Result success( ) {
        return  result(200,"success", null);
    }

   public static Result fail(int code,String msg) {return result(code, msg,null);}


}
