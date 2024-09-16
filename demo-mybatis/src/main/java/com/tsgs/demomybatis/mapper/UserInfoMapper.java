package com.tsgs.demomybatis.mapper;

import com.tsgs.demomybatis.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserInfoMapper {

    int insertUserInfo(UserInfo userInfo);

    int updateUserInfo(UserInfo userInfo);

    int deleteUserInfo(UserInfo userInfo);

    UserInfo getUserInfoById(Long userId);

    List<UserInfo> selectUserInfo(UserInfo userInfo);
}
