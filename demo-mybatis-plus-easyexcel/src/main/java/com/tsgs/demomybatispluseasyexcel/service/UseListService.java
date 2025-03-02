package com.tsgs.demomybatispluseasyexcel.service;

import org.springframework.web.multipart.MultipartFile;

public interface UseListService {

    /**
     * 导入基础类使用清单
     *
     * @param file 文件
     */
    void importBasicUseList(MultipartFile file);

    /**
     * 导入非基础类使用清单
     *
     * @param file 文件
     */
    void importNonBasicUseList(MultipartFile file);
}
