package com.tsgs.demomybatispluseasypoi.controller;

import com.tsgs.demomybatispluseasypoi.service.ExcelParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * @author liujunbo
 * @version 1.0
 * @description
 * @since 2025/4/19 13:30
 */
@RestController
@RequestMapping("/api/excel")
@Slf4j
public class ExcelParseController {

    private final ExcelParseService excelParseService;

    public ExcelParseController(ExcelParseService excelParseService) {
        this.excelParseService = excelParseService;
    }

    @PostMapping("/parse")
    public String parseExcel(@RequestParam("file") MultipartFile file) {
        log.info("开始解析Excel，文件名：{}", file.getOriginalFilename());

        try {
            Set<String> orgNoSet = excelParseService.parseExcel(file);
            return orgNoSet.toString();
        } catch (Exception e) {
            log.info("解析Excel异常：{}", e.getMessage());
            return e.getMessage();
        }
    }

    @GetMapping("/hello")
    public String hello(@RequestParam("name") String name) {
        log.info("传入参数name={}", name);
        return "hello " + name;
    }
}
