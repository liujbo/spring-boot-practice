package com.tsgs.demomybatispluseasyexcel.controller;

import com.tsgs.demomybatispluseasyexcel.service.AbnormalReportService;
import com.tsgs.demomybatispluseasyexcel.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyrj/report")
@Slf4j
public class AbnormalReportController {

    @Autowired
    private AbnormalReportService abnormalReportService;

    @PostMapping("checkFile")
    public void checkFileAbnormalReport(@RequestParam("file") MultipartFile file) {

        abnormalReportService.checkFileAbnormalReport(file);
    }


    @PostMapping("checkData")
    public AjaxResult checkDataAbnormalReport(@RequestBody Object object) {
        return abnormalReportService.checkDataAbnormalReport(object);
    }
}
