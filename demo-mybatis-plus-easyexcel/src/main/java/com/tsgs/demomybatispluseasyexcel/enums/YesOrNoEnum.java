package com.tsgs.demomybatispluseasyexcel.enums;

import lombok.Getter;

@Getter
public enum YesOrNoEnum {
    YES("1", "是"),
    NO("0", "否");

    private final String value;
    private final String description;

    YesOrNoEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
