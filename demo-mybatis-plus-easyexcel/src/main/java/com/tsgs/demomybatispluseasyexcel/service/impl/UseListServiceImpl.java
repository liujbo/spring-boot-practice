package com.tsgs.demomybatispluseasyexcel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.tsgs.demomybatispluseasyexcel.domain.KyrjBasicUseListDomain;
import com.tsgs.demomybatispluseasyexcel.domain.KyrjNonBasicUseListDomain;
import com.tsgs.demomybatispluseasyexcel.listener.BasicUseListDomainListener;
import com.tsgs.demomybatispluseasyexcel.listener.NonBasicUseListDomainListener;
import com.tsgs.demomybatispluseasyexcel.mapper.KyrjBasicUseListDomainMapper;
import com.tsgs.demomybatispluseasyexcel.mapper.KyrjNonBasicUseListDomainMapper;
import com.tsgs.demomybatispluseasyexcel.service.UseListService;
import com.tsgs.demomybatispluseasyexcel.util.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 使用清单业务service
 */
@Service
@Slf4j
public class UseListServiceImpl implements UseListService {

    @Autowired
    private KyrjBasicUseListDomainMapper basicUseListDomainMapper;

    @Autowired
    private KyrjNonBasicUseListDomainMapper nonbasicUseListDomainMapper;

    /**
     * 导入基础类使用清单
     *
     * @param file 文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importBasicUseList(MultipartFile file) {
        log.info("导入基础类使用清单");

        BasicUseListDomainListener basicUseListDomainListener = new BasicUseListDomainListener();
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, KyrjBasicUseListDomain.class, basicUseListDomainListener).sheet().doRead();
        } catch (IOException e) {
            throw new BizException("导入基础类使用清单" + e.getMessage());
        }

        List<KyrjBasicUseListDomain> basicUseListDomainList = basicUseListDomainListener.getBasicUseListDomainList();
        if (CollectionUtils.isEmpty(basicUseListDomainList)) {
            throw new BizException("导入文件为空，请检查");
        }
        basicUseListDomainMapper.insert(basicUseListDomainList);
    }

    /**
     * 导入非基础类使用清单
     *
     * @param file 文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importNonBasicUseList(MultipartFile file) {
        log.info("导入非基础类使用清单");

        NonBasicUseListDomainListener nonBasicUseListDomainListener = new NonBasicUseListDomainListener();

        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, KyrjNonBasicUseListDomain.class, nonBasicUseListDomainListener).sheet().doRead();
        } catch (IOException e) {
            throw new BizException("导入非基础类使用清单" + e.getMessage());
        }

        List<KyrjNonBasicUseListDomain> nonBasicUseListDomainList = nonBasicUseListDomainListener.getNonBasicUseListDomainList();
        if (CollectionUtils.isEmpty(nonBasicUseListDomainList)) {
            throw new BizException("导入文件为空，请检查");
        }
        nonbasicUseListDomainMapper.insert(nonBasicUseListDomainList);
    }
}
