package com.tsgs.demotkmybatis.mapper;

import com.alibaba.fastjson2.JSON;
import com.tsgs.demotkmybatis.entity.TbUserInfo;
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
class TbUserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
        TbUserInfo tbUserInfo = new TbUserInfo();
        tbUserInfo.setUserNo("293001");
        tbUserInfo.setUserName("奥巴马");
        tbUserInfo.setUserGender("1");
        tbUserInfo.setUserBirthday(new Date());
        tbUserInfo.setUserContact("23232323");
        userInfoMapper.insert(tbUserInfo);
    }

    @Test
    public void testFindAll() {
        List<TbUserInfo> tbUserInfoList = userInfoMapper.selectAll();
        Assert.notEmpty(tbUserInfoList, "用户信息为空");
        log.info("查询全部用户信息 [userInfoList]  = {}", JSON.toJSONString(tbUserInfoList));
    }

    @Test
    public void testFindByParam() {
        Example example = new Example(TbUserInfo.class);
        example.createCriteria().andEqualTo("userNo", "293001");

        List<TbUserInfo> tbUserInfoList = userInfoMapper.selectByExample(example);
        Assert.notEmpty(tbUserInfoList, "用户信息为空");
        log.info("根据条件查询用户列表 [userInfoList]  = {}", JSON.toJSONString(tbUserInfoList));
    }
}