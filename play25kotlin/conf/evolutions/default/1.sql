# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table plan_task (
  id                            bigint auto_increment not null,
  require_seq                   tinyint(1) DEFAULT '1' COMMENT '是否要求顺序执行',
  seq_type                      varchar(64) DEFAULT 'global_seq' COMMENT '顺序执行的类别' not null,
  plan_run_time                 DATETIME COMMENT '任务计划执行时间' not null,
  task_status                   INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Error' not null,
  class_name                    varchar(1024) COMMENT 'Runnable task class name' not null,
  json_data                     TEXT COMMENT 'Runnable task class json data' not null,
  tag                           TEXT COMMENT '标签,用于保存任务相关的额外数据',
  remarks                       TEXT COMMENT '发生异常情况的时候, 用于记录额外信息',
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_plan_task primary key (id)
);


# --- !Downs

drop table if exists plan_task;

