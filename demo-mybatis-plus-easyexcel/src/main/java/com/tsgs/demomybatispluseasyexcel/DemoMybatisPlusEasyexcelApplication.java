package com.tsgs.demomybatispluseasyexcel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.tsgs.demomybatispluseasyexcel.mapper")
public class DemoMybatisPlusEasyexcelApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoMybatisPlusEasyexcelApplication.class, args);
    }

}
