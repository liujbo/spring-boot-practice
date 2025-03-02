package com.tsgs.demomybatispluseasyexcel.service;

import com.tsgs.demomybatispluseasyexcel.util.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

public interface AbnormalReportService {

    /**
     * 传入文件生成异常报告
     *
     * @param file 待检查数据文件
     */
    void checkFileAbnormalReport(MultipartFile file);

    /**
     * 传入数据返回异常数据
     *
     * @param object 待检查数据对象
     * @return 异常数据
     */
    AjaxResult checkDataAbnormalReport(Object object);
}
