package com.tsgs.demotkmybatis.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.Date;


@Entity
@Table(name = "tb_user_info")
public class UserInfo {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public Date getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(Date userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getUserContact() {
        return userContact;
    }

    public void setUserContact(String userContact) {
        this.userContact = userContact;
    }

    public String getUserHomeAddress() {
        return userHomeAddress;
    }

    public void setUserHomeAddress(String userHomeAddress) {
        this.userHomeAddress = userHomeAddress;
    }

    public String getUserCompanyAddress() {
        return userCompanyAddress;
    }

    public void setUserCompanyAddress(String userCompanyAddress) {
        this.userCompanyAddress = userCompanyAddress;
    }
}