package com.tsgs.demomybatis.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class UserInfo {

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 用户编号
     */
    private String userNo;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 性别
     */
    private String userGender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date userBirthday;

    /**
     * 联系电话
     */
    private String userContact;

    /**
     * 家庭地址
     */
    private String userHomeAddress;

    /**
     * 公司地址
     */
    private String userCompanyAddress;
}
