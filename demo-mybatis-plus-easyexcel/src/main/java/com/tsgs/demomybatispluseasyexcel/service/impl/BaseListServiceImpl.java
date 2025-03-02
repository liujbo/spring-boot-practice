package com.tsgs.demomybatispluseasyexcel.service.impl;

import com.tsgs.demomybatispluseasyexcel.service.BaseListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class BaseListServiceImpl implements BaseListService {
    /**
     * 导入基础类基线清单
     *
     * @param file 导入文件
     */
    @Override
    public void importBasicBaseList(MultipartFile file) {

    }

    /**
     * 导入非基础类基线清单
     *
     * @param file 导入文件
     */
    @Override
    public void importNonBasicBaseList(MultipartFile file) {

    }
}
