package com.tsgs.demomybatisplus.mapper;

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
        userInfo.setId(1338187L);
        userInfo.setUserContact("1111电话号码");
        userInfo.setUserHomeAddress("1111黑贝声天安门");
        int result = userInfoMapper.updateById(userInfo);
        Assert.isTrue(1 == result, "更新用户信息失败");
    }
}