package com.tsgs.demotkmybatiseasyexcel.controller;

import com.alibaba.fastjson2.JSON;
import com.tsgs.demotkmybatiseasyexcel.annotation.LogExecutionTime;
import com.tsgs.demotkmybatiseasyexcel.annotation.LogRecord;
import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import com.tsgs.demotkmybatiseasyexcel.service.UserInfoService;
import com.tsgs.demotkmybatiseasyexcel.util.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/userinfo")
@Slf4j
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 用户信息导入
     *
     * @param file 导入文件对象
     */
    @PostMapping("/import")
    @LogRecord(methodName = "用户信息导入")
    @LogExecutionTime
    public AjaxResult userInfoImport(@RequestParam("file") MultipartFile file) {
        log.info("用户信息导入测试");
        userInfoService.userInfoImport(file);
        return AjaxResult.success();
    }

    /**
     * 用户信息导出
     *
     * @param response 响应对象
     * @param object   条件参数
     */
    @PostMapping("/export")
    public void userInfoImport(HttpServletResponse response, @RequestBody Object object) {
        log.info("用户信息导出测试");
        userInfoService.userInfoExport(response, object);
    }

    /**
     * 查询用户信息
     *
     * @param userInfo 用户信息条件对象
     * @return 用户信息列表
     */
    @PostMapping("list")
    @LogRecord(methodName = "用户信息查询")
    @LogExecutionTime
    public AjaxResult userInfoList(@RequestBody TbUserInfo userInfo) {
        log.info("查询用户信息列表传入参数：{}", JSON.toJSONString(userInfo));
        return userInfoService.selectUserInfo(userInfo);
    }
}
