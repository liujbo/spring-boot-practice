package com.tsgs.demotkmybatiseasyexcel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import com.tsgs.demotkmybatiseasyexcel.listener.UserInfoListener;
import com.tsgs.demotkmybatiseasyexcel.mapper.UserInfoMapper;
import com.tsgs.demotkmybatiseasyexcel.service.UserInfoService;
import com.tsgs.demotkmybatiseasyexcel.util.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 用户信息导入
     *
     * @param file 导入文件对象
     */
    @Override
    public void userInfoImport(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            EasyExcel.read(inputStream, TbUserInfo.class, new UserInfoListener(userInfoMapper)).sheet().doRead();
        } catch (IOException e) {
            log.error("解析异常", e);
        }
    }

    /**
     * 用户信息导出
     *
     * @param response 响应对象
     * @param object   条件参数
     */
    @Override
    public void userInfoExport(HttpServletResponse response, Object object) {


    }

    @Override
    public AjaxResult selectUserInfo(TbUserInfo userInfo) {
        Example example = new Example(TbUserInfo.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.hasLength(userInfo.getUserName())) {
            criteria.andLike("userName", "%" + userInfo.getUserName() + "%");
        }
        if (StringUtils.hasLength(userInfo.getUserHomeAddress())) {
            criteria.andLike("userHomeAddress", "%" + userInfo.getUserHomeAddress() + "%");
        }
        List<TbUserInfo> tbUserInfoList = userInfoMapper.selectByExample(example);
        return AjaxResult.success(tbUserInfoList);
    }
}
