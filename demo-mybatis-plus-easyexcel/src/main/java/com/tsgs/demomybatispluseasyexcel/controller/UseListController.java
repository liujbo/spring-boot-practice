package com.tsgs.demomybatispluseasyexcel.controller;

import com.tsgs.demomybatispluseasyexcel.enums.YesOrNoEnum;
import com.tsgs.demomybatispluseasyexcel.service.UseListService;
import com.tsgs.demomybatispluseasyexcel.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyrj/uselist")
@Slf4j
public class UseListController {

    @Autowired
    private UseListService useListService;

    @PostMapping("/import")
    public AjaxResult importUseList(@RequestParam("file") MultipartFile file, @RequestParam("softwareType") String softwareType) {
        if (YesOrNoEnum.YES.getValue().equals(softwareType)) {
            useListService.importBasicUseList(file);
        } else {
            useListService.importNonBasicUseList(file);
        }
        return AjaxResult.success("导入成功");
    }


}
