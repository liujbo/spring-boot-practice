package com.tsgs.demotkmybatis.mapper;


import com.tsgs.demotkmybatis.entity.TbUserInfo;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.ExampleMapper;

@Mapper
public interface UserInfoMapper extends BaseMapper<TbUserInfo>, ExampleMapper<TbUserInfo> {
}
