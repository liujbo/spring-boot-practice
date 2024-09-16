package com.tsgs.demotkmybatis.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_user_info")
public class TbUserInfo {

    /**
     * 自增主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "JDBC")
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
    private String userName;

    /**
     * 性别
     */
    @Column(name = "user_gender")
    private String userGender;

    /**
     * 出生日期
     */
    @Column(name = "user_birthday")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date userBirthday;

    /**
     * 联系电话
     */
    @Column(name = "user_contact")
    private String userContact;

    /**
     * 家庭地址
     */
    @Column(name = "user_home_address")
    private String userHomeAddress;

    /**
     * 公司地址
     */
    @Column(name = "user_company_address")
    private String userCompanyAddress;
}