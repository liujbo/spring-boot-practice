package com.tsgs.demotkmybatiseasyexcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = {"com.tsgs.demotkmybatiseasyexcel.mapper"})
@SpringBootApplication
public class DemoTkmybatisEasyexcelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoTkmybatisEasyexcelApplication.class, args);
    }
}
