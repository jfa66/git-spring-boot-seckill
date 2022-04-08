package com.example.seckill.entity;

import lombok.Data;

import java.util.HashMap;

/**
 * 响应类
 *
 * @author jiangfengan
 */
@Data
public class Result extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static Result error() {
        return error("500", "未知错误");
    }

    public static Result error(String msg) {
        return error("500", msg);
    }

    public static Result error(String code, String msg) {
        Result result = new Result();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    public static Result ok() {
        return ok("成功");
    }

    public static Result ok(String msg) {
        Result r = new Result();
        r.put("code", "200");
        r.put("msg", msg);
        return r;
    }

    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
