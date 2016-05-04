# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table k_event_exception (
  id                        bigint auto_increment not null,
  bus_name                  VARCHAR(1024) COMMENT 'Event Bus Name',
  event_class               VARCHAR(1024) COMMENT 'Event Object Class Name',
  event_json                TEXT COMMENT 'Event Object Json Data',
  subscriber_class          VARCHAR(1024) COMMENT 'Event Subscriber Class Name',
  subscriber_method         VARCHAR(256) COMMENT 'Event Subscriber Method Name',
  exception                 TEXT COMMENT 'Exception Message',
  constraint pk_k_event_exception primary key (id))
;

create table k_attachment (
  id                        bigint auto_increment not null,
  file_name                 VARCHAR(256) DEFAULT '' COMMENT '附件材料名称,即扫描件文件名称',
  file_path                 VARCHAR(4096) DEFAULT '' COMMENT '附件材料文件路径,即文件在文件服务器上的文件路径',
  content_type              VARCHAR(64) COMMENT '文件对应的 ContentType, 目前支持:image/JPEG image/PNG',
  create_time               TIMESTAMP NOT NULL DEFAULT NOW(),
  scale_path_big            VARCHAR(4096) DEFAULT '' COMMENT '图片附件大缩略图路径,即文件在文件服务器上的文件路径',
  scale_path_middle         VARCHAR(4096) DEFAULT '' COMMENT '图片附件中缩略图路径,即文件在文件服务器上的文件路径',
  scale_path_small          VARCHAR(4096) DEFAULT '' COMMENT '图片附件小缩略图路径,即文件在文件服务器上的文件路径',
  remarks                   TEXT COMMENT '备注信息',
  constraint pk_k_attachment primary key (id))
;

create table plan_task (
  id                        bigint auto_increment not null,
  require_seq               TINYINT(1) DEFAULT '1' COMMENT '是否要求顺序执行',
  seq_type                  VARCHAR(64) DEFAULT 'global_seq' COMMENT '顺序执行的类别' not null,
  create_time               DATETIME COMMENT '任务创建时间' not null,
  plan_run_time             DATETIME COMMENT '任务计划执行时间' not null,
  task_status               INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Exception' not null,
  class_name                VARCHAR(1024) COMMENT 'Runable task class name' not null,
  json_data                 TEXT COMMENT 'Runable task class json data' not null,
  tag                       TEXT COMMENT '标签,用于保存任务相关的额外数据',
  remarks                   TEXT COMMENT '发生异常情况的时候, 用于记录额外信息',
  constraint pk_plan_task primary key (id))
;

create table sample_model (
  id                        bigint auto_increment not null,
  plan_run_time             DATETIME COMMENT '任务计划执行时间',
  constraint pk_sample_model primary key (id))
;

create table k_sys_conf (
  id                        bigint auto_increment not null,
  conf_key                  VARCHAR(128) COMMENT '配置项名称' not null,
  conf_value                VARCHAR(256) COMMENT '配置项值',
  ext_info                  VARCHAR(1024) COMMENT '配置项备注',
  constraint uq_k_sys_conf_conf_key unique (conf_key),
  constraint pk_k_sys_conf primary key (id))
;

create table k_user (
  id                        bigint auto_increment not null,
  user_id                   CHAR(40) COMMENT '用户 UUID' not null,
  user_name                 VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  password                  VARCHAR(128) COMMENT '二次防字典工具加密后的密码',
  mobile                    VARCHAR(16) DEFAULT NULL COMMENT '用户手机',
  email                     VARCHAR(64) DEFAULT NULL COMMENT '用户邮箱',
  weixin_id                 VARCHAR(64) DEFAULT NULL COMMENT '微信号',
  real_name                 VARCHAR(32) DEFAULT NULL COMMENT '真实姓名',
  id_no                     VARCHAR(18) DEFAULT NULL COMMENT '身份证号码',
  ext_attr                  TEXT DEFAULT NULL COMMENT '扩展属性',
  constraint uq_k_user_user_id unique (user_id),
  constraint uq_k_user_user_name unique (user_name),
  constraint uq_k_user_mobile unique (mobile),
  constraint uq_k_user_email unique (email),
  constraint uq_k_user_weixin_id unique (weixin_id),
  constraint uq_k_user_real_name unique (real_name),
  constraint uq_k_user_id_no unique (id_no),
  constraint pk_k_user primary key (id))
;

create table api_server_boot (
  id                        bigint auto_increment not null,
  svn_version               VARCHAR(256) COMMENT 'ApiServer 的 svn 版本号',
  boot_time                 DATETIME COMMENT 'ApiServer的启动时间',
  stop_time                 DATETIME COMMENT 'ApiServer的停止时间',
  run_time_desc             VARCHAR(256) COMMENT 'ApiServer 运行时间描述',
  constraint pk_api_server_boot primary key (id))
;

create table api_log (
  id                        bigint auto_increment not null,
  guid                      CHAR(40) COMMENT 'log GUID' not null,
  log_time                  DATETIME COMMENT 'log 记录的时间',
  api_method                VARCHAR(8) DEFAULT '' COMMENT 'GET or POST',
  api_path                  VARCHAR(1024) DEFAULT '' COMMENT 'api path',
  api_url                   VARCHAR(1024) DEFAULT '' COMMENT 'api url',
  form_data                 TEXT COMMENT 'form_data',
  spend_time                BIGINT DEFAULT -1 COMMENT 'api 执行话费的时间, 单位:milliseconds',
  host_name                 VARCHAR(64) DEFAULT '' COMMENT 'play 实例所在的主机名',
  client_ip                 VARCHAR(32) DEFAULT '' COMMENT '客户端 ip',
  exceptions                TEXT COMMENT '发生异常时记录的异常信息',
  constraint uq_api_log_guid unique (guid),
  constraint pk_api_log primary key (id))
;




# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table k_event_exception;

drop table k_attachment;

drop table plan_task;

drop table sample_model;

drop table k_sys_conf;

drop table k_user;

drop table api_server_boot;

drop table api_log;

SET FOREIGN_KEY_CHECKS=1;

