package com.tsgs.demotkmybatiseasyexcel.util;

import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object> {

    @Serial
    private static final long serialVersionUID = 1L;

    public AjaxResult() {
    }

    public AjaxResult(String message, int code) {
        put("message", message);
        put("code", code);
    }

    public AjaxResult(String message, int code, Object data) {
        put("message", message);
        put("code", code);
        put("data", data);
    }

    public static AjaxResult success() {
        return new AjaxResult("success", HttpStatus.OK.value());
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult("success", HttpStatus.OK.value(), data);
    }

    public static AjaxResult success(String message) {
        return new AjaxResult(message, HttpStatus.OK.value());
    }

    public static AjaxResult success(String message, int code, Object data) {
        return new AjaxResult(message, code, data);
    }

    public static AjaxResult error() {
        return new AjaxResult("error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public static AjaxResult error(String message, int code) {
        return new AjaxResult(message, code);
    }

    public static AjaxResult error(String message, int code, Object data) {
        return new AjaxResult(message, code, data);
    }
}
