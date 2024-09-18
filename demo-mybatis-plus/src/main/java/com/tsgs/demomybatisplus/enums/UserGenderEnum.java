package com.tsgs.demomybatisplus.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 性别枚举
 */
public enum UserGenderEnum {

    MALE("M", "男"),
    FEMALE("F", "女");

    @EnumValue
    private final String code;
    private final String desc;

    UserGenderEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
