package com.tsgs.demotkmybatiseasyexcel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import com.tsgs.demotkmybatiseasyexcel.mapper.UserInfoMapper;
import com.tsgs.demotkmybatiseasyexcel.util.UserIdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserInfoListener extends AnalysisEventListener<TbUserInfo> {

    private final List<TbUserInfo> userInfoList = new ArrayList<>();

    @Getter
    @Setter
    private UserInfoMapper userInfoMapper;

    @Override
    public void invoke(TbUserInfo data, AnalysisContext context) {
        data.setUserNo(UserIdGenerator.generateUserId());
        userInfoList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("全部解析完成");
        userInfoMapper.insertList(userInfoList);
        log.info("=======导入成功");
    }

    public UserInfoListener(UserInfoMapper userInfoMapper) {
        this.userInfoMapper = userInfoMapper;
    }
}
