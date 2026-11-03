package com.example.stallsystem.common;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    /** 业务状态码：200成功，500失败，401未登录 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 数据 */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
