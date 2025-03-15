package com.tsgs.demomybatispluseasyexcel.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 非基础类使用清单信息对象
 */
@Data
@TableName(value = "kyrj_non_basic_use_list")
public class KyrjNonBasicUseListDomain {

    @TableId(type = IdType.AUTO)
    @ExcelIgnore
    private Long id;

    @ExcelProperty(value = "软件英文名称", index = 0)
    private String softwareName;

    @ExcelProperty(value = "软件版本号", index = 1)
    private String softwareVersion;

    @ExcelProperty(value = "开源许可证", index = 2)
    private String openLicense;

    @ExcelProperty(value = "使用产品英文简称", index = 3)
    private String usageProduct;

    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT)
    private Date startUseDate;

    @ExcelIgnore
    private Date stopUseDate;

    @ExcelIgnore
    private String operateState;

    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ExcelIgnore
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}
