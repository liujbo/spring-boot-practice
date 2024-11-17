package com.tsgs.demotkmybatiseasyexcel.annotation;

import java.lang.annotation.*;

/**
 * 计算接口耗时注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
