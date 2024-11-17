package com.tsgs.demotkmybatiseasyexcel.mapper;

import com.tsgs.demotkmybatiseasyexcel.entity.TbLogRecord;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

public interface LogRecordMapper extends Mapper<TbLogRecord>, IdsMapper<TbLogRecord>, MySqlMapper<TbLogRecord> {
}
