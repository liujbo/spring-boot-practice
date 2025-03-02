package com.tsgs.demomybatispluseasyexcel.util;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.nio.file.Paths;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/practice?serverTimezone=Asia/Shanghai&remarks=true&useInformationSchema=true", "root", "Soldier@53")

                // 全局配置
                .globalConfig(builder -> builder
                        .outputDir(Paths.get(System.getProperty("user.dir"), "src", "main", "java").toString())
                        .author("jbliu")
                        .dateType(DateType.ONLY_DATE)
                        .commentDate("yyyy-MM-dd")
                        .disableOpenDir() // 设置生成后不自动打开目录
                        .disableServiceInterface()
                )

                // 包配置
                .packageConfig(builder -> builder
                        .parent("com.tsgs.demomybatispluseasyexcel") // 设置父包名
                        .entity("domain")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                )

                // 策略配置
                .strategyConfig(builder -> builder
                        .addInclude("kyrj_basic_base_list,kyrj_non_basic_base_list")
                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(new Column("create_time", FieldFill.INSERT))

                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
