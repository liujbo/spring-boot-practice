package com.tsgs.demomybatispluseasyexcel.service.impl;

import com.tsgs.demomybatispluseasyexcel.service.AbnormalReportService;
import com.tsgs.demomybatispluseasyexcel.util.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class AbnormalReportServiceImpl implements AbnormalReportService {

    /**
     * 传入文件生成异常报告
     *
     * @param file 待检查数据文件
     */
    @Override
    public void checkFileAbnormalReport(MultipartFile file) {

    }

    /**
     * 传入数据返回异常数据
     *
     * @param object 待检查数据对象
     * @return 异常数据
     */
    @Override
    public AjaxResult checkDataAbnormalReport(Object object) {
        return null;
    }
}
