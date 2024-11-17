package com.tsgs.demotkmybatiseasyexcel.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Table(name = "tb_log_record")
public class TbLogRecord {

    @Id
    private Long id;

    @Column(name = "method")
    private String method;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "create_date")
    private Date createDate;

    public TbLogRecord(String method, String methodName) {
        this.method = method;
        this.methodName = methodName;
    }
}
