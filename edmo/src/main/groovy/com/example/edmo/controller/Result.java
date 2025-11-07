package com.example.edmo.controller;

import lombok.Data;

@Data
public class Result {
    int code;
    String msg;
    long total;
    Object data;

    private static Result result(int code, String msg,long total, Object data) {
        Result result = new Result();
        result.code = code;
        result.msg = msg;
        result.total=total;
        result.data = data;
        return result;
    }

   public static Result success(long total, Object data) {
        return  result(200,"success",total,data);
   }
   public static Result success(Object data ) {
        return  result(200,"success", 0,data);
   }

    public static Result success( ) {
        return  result(200,"success", 0,null);
    }

   public static Result fail(String msg) {return result(500, msg,0,null);}


}
