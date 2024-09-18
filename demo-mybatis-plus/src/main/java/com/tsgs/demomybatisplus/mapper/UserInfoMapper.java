package com.tsgs.demomybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tsgs.demomybatisplus.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
