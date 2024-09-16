package com.tsgs.demomybatis.mapper;

import com.alibaba.fastjson2.JSON;
import com.tsgs.demomybatis.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
class UserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
        UserInfo userInfo = UserInfo.builder().userNo("077647").userName("王林").userBirthday(new Date()).userGender("1").userContact("13829330412").userHomeAddress("陕西省延安市宝塔山").build();
        userInfoMapper.insertUserInfo(userInfo);
    }

    @Test
    void updateUserInfo() {
        UserInfo userInfo = UserInfo.builder().id(1L).userName("张三").build();
        userInfoMapper.updateUserInfo(userInfo);

        UserInfo userInfo1 = UserInfo.builder().userName("张三").build();
        List<UserInfo> userInfoList = userInfoMapper.selectUserInfo(userInfo1);
        Assert.notEmpty(userInfoList, "更新用户名称失败");
        log.info("[userInfo] : {}", JSON.toJSONString(userInfoList.get(0)));
    }

    @Test
    void deleteUserInfo() {
        UserInfo userInfo = UserInfo.builder().id(1L).build();
        userInfoMapper.deleteUserInfo(userInfo);

        UserInfo userInfo1 = userInfoMapper.getUserInfoById(1L);
        Assert.isNull(userInfo1, "删除用户失败");
    }

    @Test
    void getUserInfoById() {
        UserInfo userInfo = userInfoMapper.getUserInfoById(1L);
        Assert.notNull(userInfo, "获取用户信息为空");
        log.info("[userInfo]:{}", JSON.toJSONString(userInfo));
    }

    @Test
    void selectUserInfo() {
        UserInfo userInfo = UserInfo.builder().build();
        List<UserInfo> userInfoList = userInfoMapper.selectUserInfo(userInfo);
        Assert.notEmpty(userInfoList, "用户列表信息为空");
        log.info("[userInfoList]:{}", JSON.toJSONString(userInfoList));
    }
}