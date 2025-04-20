package com.tsgs.demomybatispluseasyexcel.controller.excel;

import com.tsgs.demomybatispluseasyexcel.service.excel.ExcelParseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * @author liujunbo
 * @version 1.0
 * @description
 * @since 2025/4/19 20:43
 */
@RestController
@RequestMapping("/api/excel")
@Slf4j
public class ExcelParseController {

    private final ExcelParseService excelParseService;

    public ExcelParseController(ExcelParseService excelParseService) {
        this.excelParseService = excelParseService;
    }

    /**
     * 解析Excel获取去重后的机构编号
     *
     * @param file 文件对象
     * @return 去重后机构号信息
     */
    @PostMapping("/parse")
    public String parseExcel(@RequestParam("file") MultipartFile file) {
        Set<String> stringSet = excelParseService.parseExcel(file);
        if (CollectionUtils.isEmpty(stringSet)) {
            return "机构编号为空";
        }
        return stringSet.toString();
    }

    /**
     * 根据传入文件，过滤出满足条件的数据行
     *
     * @param file     传入文件
     * @param response 响应信息
     * @return 结果信息
     */
    @PostMapping("/parseanddownload")
    public String parseAndDownload(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=safe_output.xlsx");
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "max-age=0");
        excelParseService.parseAndDownload(file, response);
        return "操作成功";
    }
}
