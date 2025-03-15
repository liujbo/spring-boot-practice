create table kyrj_basic_base_list
(
    id               bigint auto_increment comment '自增主键' primary key,
    software_name    varchar(50)  default null comment '软件英文名称',
    software_version varchar(50)  default null comment '软件版本号',
    open_license     varchar(100) default null comment '开源许可证',
    index name_version (software_name, software_version) comment '软件名称和版本号联合索引'
) comment '开源软件基础类基线清单' collate = utf8mb4_general_ci;

create table kyrj_non_basic_base_list
(
    id                         bigint auto_increment comment '自增主键' primary key,
    software_name              varchar(50)  default null comment '软件英文名称',
    software_version           varchar(50)  default null comment '软件版本号',
    open_license               varchar(100) default null comment '开源许可证',
    package_type               varchar(4)   default null comment '软件包类型',
    package_location_parameter varchar(200) default null comment '软件定位参数',
    index name_version (software_name, software_version) comment '软件名称和版本号联合索引'
) comment '开源软件非基础类基线清单' collate = utf8mb4_general_ci;

create table kyrj_basic_use_list
(
    id               bigint auto_increment comment '自增主键' primary key,
    software_name    varchar(50)  default null comment '软件英文名称',
    software_version varchar(50)  default null comment '软件版本号',
    open_license     varchar(100) default null comment '开源许可证',
    usage_product    varchar(50)  default null comment '使用子产品英文名称',
    start_use_date   datetime     default null comment '开始使用日期',
    stop_use_date    datetime     default null comment '停止使用日期',
    operate_state    varchar(4)   default null comment '操作来源标识',
    index name_version (software_name, software_version, usage_product) comment '软件名称和版本号和使用子产品英文名称联合索引'
) comment '开源软件非基础类使用清单' collate = utf8mb4_general_ci;

create table kyrj_non_basic_use_list
(
    id               bigint auto_increment comment '自增主键' primary key,
    software_name    varchar(50)  default null comment '软件英文名称',
    software_version varchar(50)  default null comment '软件版本号',
    open_license     varchar(100) default null comment '开源许可证',
    usage_product    varchar(50)  default null comment '使用子产品英文名称',
    start_use_date   datetime     default null comment '开始使用日期',
    stop_use_date    datetime     default null comment '停止使用日期',
    operate_state    varchar(4)   default null comment '操作来源标识',
    create_time datetime default null comment '创建时间',
    update_time datetime default null comment '更新时间',
    index name_version (software_name, software_version, usage_product) comment '软件名称和版本号和使用子产品英文名称联合索引'
) comment '开源软件非基础类使用清单' collate = utf8mb4_general_ci;