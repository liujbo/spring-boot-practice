package com.tsgs.demomybatispluseasyexcel.service;

import org.springframework.web.multipart.MultipartFile;

public interface BaseListService {

    /**
     * 导入基础类基线清单
     *
     * @param file 导入文件
     */
    void importBasicBaseList(MultipartFile file);

    /**
     * 导入非基础类基线清单
     *
     * @param file 导入文件
     */
    void importNonBasicBaseList(MultipartFile file);
}
