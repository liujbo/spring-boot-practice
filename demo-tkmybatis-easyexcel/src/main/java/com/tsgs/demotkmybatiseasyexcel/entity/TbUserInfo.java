package com.tsgs.demotkmybatiseasyexcel.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Table(name = "tb_user_info")
public class TbUserInfo {

    /**
     * 自增主键ID
     */
    @Id
    private Long id;

    /**
     * 用户编号
     */
    @Column(name = "user_no")
    private String userNo;

    /**
     * 用户姓名
     */
    @Column(name = "user_name")
    @ExcelProperty(value = "用户姓名")
    private String userName;

    /**
     * 性别
     */
    @Column(name = "user_gender")
    @ExcelProperty(value = "性别")
    private String userGender;

    /**
     * 出生日期
     */
    @Column(name = "user_birthday")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelProperty(value = "出生日期")
    @DateTimeFormat(value = "yyyy-MM-dd")
    private Date userBirthday;

    /**
     * 联系电话
     */
    @Column(name = "user_contact")
    @ExcelProperty(value = "联系方式")
    private String userContact;

    /**
     * 家庭地址
     */
    @Column(name = "user_home_address")
    @ExcelProperty(value = "家庭住址")
    private String userHomeAddress;

    /**
     * 公司地址
     */
    @Column(name = "user_company_address")
    private String userCompanyAddress;
}