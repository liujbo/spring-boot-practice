package com.tsgs.demotkmybatis.mapper;

import com.alibaba.fastjson2.JSON;
import com.tsgs.demotkmybatis.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
class UserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserNo("293001");
        userInfo.setUserName("奥巴马");
        userInfo.setUserGender("1");
        userInfo.setUserBirthday(new Date());
        userInfo.setUserContact("23232323");
        userInfoMapper.insert(userInfo);
    }

    @Test
    public void testFindAll() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
        Assert.notEmpty(userInfoList, "用户信息为空");
        log.info("查询全部用户信息 [userInfoList]  = {}", JSON.toJSONString(userInfoList));
    }

    @Test
    public void testFindByParam() {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("userNo", "293001");

        List<UserInfo> userInfoList = userInfoMapper.selectByExample(example);
        Assert.notEmpty(userInfoList, "用户信息为空");
        log.info("根据条件查询用户列表 [userInfoList]  = {}", JSON.toJSONString(userInfoList));
    }
}