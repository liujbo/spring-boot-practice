package com.tsgs.demomybatispluseasyexcel.util;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public AjaxResult exceptionHandler(Exception e) {
        return AjaxResult.error("程序异常:" + e.getMessage());
    }

    @ExceptionHandler(value = BizException.class)
    public AjaxResult exceptionHandler(BizException e) {
        return AjaxResult.error("业务异常:" + e.getMessage());
    }
}
