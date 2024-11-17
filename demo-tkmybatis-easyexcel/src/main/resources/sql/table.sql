create table tb_log_record
(
    id          bigint auto_increment comment '自增主键'
        primary key,
    path        varchar(100) default null comment '请求接口路径',
    method      varchar(100) default null comment '方法名称',
    method_name varchar(100) default null comment '方法名称',
    method_type varchar(10)  default null comment '请求类型',
    create_date datetime null comment '插入时间'
) comment '操作记录表';


create table tb_user_info
(
    id                   bigint auto_increment comment '自增主键'
        primary key,
    user_no              varchar(20)  not null comment '用户编号',
    user_name            varchar(20)  not null comment '用户姓名',
    user_gender          char(2)      not null comment '用户性别',
    user_birthday        datetime     not null comment '用户出生年月',
    user_contact         varchar(20)  null comment '用户联系方式',
    user_home_address    varchar(100) null comment '用户家庭住址',
    user_company_address varchar(100) null comment '用户公司住址',
    create_date          datetime     null comment '插入时间',
    update_date          datetime     null comment '更新时间'
) comment '公共用户信息表';