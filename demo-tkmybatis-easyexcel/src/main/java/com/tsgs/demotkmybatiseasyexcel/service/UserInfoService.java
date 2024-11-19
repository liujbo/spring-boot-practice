package com.tsgs.demotkmybatiseasyexcel.service;

import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import com.tsgs.demotkmybatiseasyexcel.util.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserInfoService {

    /**
     * 用户信息导入
     *
     * @param file 导入文件对象
     */
    void userInfoImport(MultipartFile file);

    /**
     * 用户信息导出
     *
     * @param response 响应对象
     * @param object   条件参数
     */
    void userInfoExport(HttpServletResponse response, Object object);

    /**
     * 查询用户信息
     *
     * @param userInfo 用户信息条件对象
     * @return 用户信息列表
     */
    AjaxResult selectUserInfo(TbUserInfo userInfo);
}
