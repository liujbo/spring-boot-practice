package com.tsgs.demomybatispluseasyexcel.controller;

import com.tsgs.demomybatispluseasyexcel.enums.YesOrNoEnum;
import com.tsgs.demomybatispluseasyexcel.service.BaseListService;
import com.tsgs.demomybatispluseasyexcel.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyrj/baselist")
@Slf4j
public class BaseListController {

    @Autowired
    private BaseListService baseListService;

    @PostMapping("/import")
    public AjaxResult importBaseList(@RequestParam("file") MultipartFile file, @RequestParam("softwareType") String softwareType) throws Exception {
        if (YesOrNoEnum.YES.getValue().equals(softwareType)) {
            baseListService.importBasicBaseList(file);
        } else {
            baseListService.importNonBasicBaseList(file);
        }
        return AjaxResult.success("导入成功");
    }
}
