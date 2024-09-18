package com.tsgs.demomybatisplus.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.tsgs.demomybatisplus.enums.UserGenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user_info")
public class UserInfo {

    /**
     * 自增主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户编号
     */
    @TableField("user_no")
    private String userNo;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 性别
     */
    @TableField("user_gender")
    @EnumValue
    private UserGenderEnum userGender;

    /**
     * 出生日期
     */
    @TableField("user_birthday")
    private Date userBirthday;

    /**
     * 联系电话
     */
    @TableField("user_contact")
    private String userContact;

    /**
     * 家庭地址
     */
    @TableField("user_home_address")
    private String userHomeAddress;

    /**
     * 公司地址
     */
    @TableField("user_company_address")
    private String userCompanyAddress;

    /**
     * 插入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateDate;
}
