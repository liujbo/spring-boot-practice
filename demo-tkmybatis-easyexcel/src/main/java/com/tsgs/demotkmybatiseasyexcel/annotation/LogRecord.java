package com.tsgs.demotkmybatiseasyexcel.annotation;

import java.lang.annotation.*;

/**
 * 自动记录操作日志注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRecord {
    String methodName() default "";
}
