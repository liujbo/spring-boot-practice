package com.tsgs.demotkmybatis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan(basePackages = {"com.tsgs.demotkmybatis.mapper"})
public class DemoTkmybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoTkmybatisApplication.class, args);
    }

}
