package com.tsgs.demotkmybatiseasyexcel.mapper;

import com.tsgs.demotkmybatiseasyexcel.entity.TbUserInfo;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface UserInfoMapper extends Mapper<TbUserInfo>, IdsMapper<TbUserInfo>, MySqlMapper<TbUserInfo> {
}
