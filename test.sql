CREATE TABLE `sys_user`
(
    `id`          bigint      NOT NULL COMMENT '主键ID',
    `name`        varchar(50) NOT NULL COMMENT '用户姓名',
    `gender`      tinyint              DEFAULT NULL COMMENT '性别 1-男 2-女 0-未知',
    `age`         tinyint unsigned     DEFAULT NULL COMMENT '年龄（无符号，范围0-255）',
    `create_time` datetime    NOT NULL COMMENT '创建时间',
    `create_by`   bigint      NOT NULL COMMENT '创建人ID',
    `update_time` datetime    NOT NULL COMMENT '更新时间',
    `update_by`   bigint      NOT NULL COMMENT '更新人ID',
    `deleted`     tinyint     NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE,
    KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统用户表';