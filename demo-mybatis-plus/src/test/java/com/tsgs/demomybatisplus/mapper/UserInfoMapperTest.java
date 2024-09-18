package com.tsgs.demomybatisplus.mapper;

import com.alibaba.fastjson2.JSON;
import com.tsgs.demomybatisplus.entity.UserInfo;
import com.tsgs.demomybatisplus.enums.UserGenderEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Date;

@Slf4j
@SpringBootTest
class UserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void testInsert() {
        UserInfo userInfo = UserInfo.builder().userNo("152423").userName("mybatis-plus测试2").userGender(UserGenderEnum.MALE).userContact("152452145").userBirthday(new Date()).build();
        userInfoMapper.insert(userInfo);
        Assert.notNull(userInfo.getId(), "插入用户信息失败");
        log.info("插入用户信息的ID是：{}", userInfo.getId());
    }

    @Test
    public void testUpdate() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(39L);
        userInfo.setUserContact("电话号码");
        userInfo.setUserHomeAddress("黑贝声天安门");
        int result = userInfoMapper.updateById(userInfo);
        Assert.isTrue(1 == result, "更新用户信息失败");
        UserInfo userInfo1 = userInfoMapper.selectById(38);
        log.info("更新用户信息后为：{}", JSON.toJSONString(userInfo1));
    }


}