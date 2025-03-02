package com.tsgs.demomybatispluseasyexcel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "kyrj_basic_base_list")
public class KyrjBasicBaseListDomain {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String softwareName;
    private String softwareVersion;
    private String openLicense;
}
