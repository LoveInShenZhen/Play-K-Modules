package models.K.BgTask;

/**
 * Created by kk on 14-1-21.
 */

import K.DB;
import K.DataDict.TaskStatus;
import K.Helper;
import com.avaje.ebean.TxCallable;
import com.avaje.ebean.TxRunnable;
import com.fasterxml.jackson.annotation.JsonFormat;
import jodd.exception.ExceptionUtil;
import play.Logger;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "plan_task")
public class PlanTask extends Model {

    @Id
    public long id;

    @Column(columnDefinition = "TINYINT(1) DEFAULT '1' COMMENT '是否要求顺序执行'")
    public boolean require_seq;

    @Column(columnDefinition = "VARCHAR(64) DEFAULT 'global_seq' COMMENT '顺序执行的类别'", nullable = false)
    public String seq_type;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "DATETIME COMMENT '任务创建时间'", nullable = false)
    public Date create_time;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'", nullable = false)
    public Date plan_run_time;

    @Column(columnDefinition = "INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Exception'", nullable = false)
    public int task_status;

    @Column(columnDefinition = "VARCHAR(256) COMMENT 'Runable task class name'", nullable = false)
    public String class_name;

    @Column(columnDefinition = "TEXT COMMENT 'Runable task class json data'", nullable = false)
    public String json_data;

    @Column(columnDefinition = "VARCHAR(256) DEFAULT '' COMMENT '标签,用于保存任务相关的额外数据'")
    public String tag;

    @Column(columnDefinition = "TEXT COMMENT '发生异常情况的时候, 用于记录额外信息'")
    public String remarks;

    public static Finder<Long, PlanTask> find = new Finder<>(Long.class, PlanTask.class);

    public static void addTask(Object obj, String seqType, boolean requireSeq, Date planRunTime) {
        addTask(obj, seqType, requireSeq, planRunTime, "");
    }

    public static void addTask(Object obj, String seqType, boolean requireSeq, Date planRunTime, String tag) {
        Date now = new Date();
        PlanTask planTask = new PlanTask();
        planTask.require_seq = requireSeq;
        planTask.seq_type = seqType;
        planTask.create_time = Helper.ZeroMillisecond(now);
        planTask.plan_run_time = Helper.ZeroMillisecond(planRunTime);
        planTask.task_status = TaskStatus.WaitingInDB.code;
        planTask.class_name = obj.getClass().getName();
        planTask.json_data = Helper.ToJsonString(obj);
        planTask.tag = tag;

        DB.ReadWriteDB().save(planTask);
    }

    public static void addTask(Object obj, String seqType, boolean requireSeq) {
        Date now = new Date();
        addTask(obj, seqType, requireSeq, now);
    }

    public static void addSingletonTask(Object obj, String seqType, boolean requireSeq, Date planRunTime, String tag) {
        String class_name = obj.getClass().getName();
        PlanTask task = PlanTask.find.where()
                .eq("class_name", class_name)
                .eq("task_status", TaskStatus.WaitingInDB.code)
                .findUnique();
        if (task == null) {
            addTask(obj, seqType, requireSeq, planRunTime, tag);
        } else {
            task.plan_run_time = planRunTime;
            DB.ReadWriteDB().save(task);
        }
    }

    public static PlanTask LoadTaskFromDb(final boolean requireSeqTask) {
        try {
            PlanTask task = DB.RunInTransaction(new TxCallable<PlanTask>() {
                @Override
                public PlanTask call() {
                    Date now = new Date();
                    PlanTask task = PlanTask.find.where()
                            .eq("require_seq", requireSeqTask)
                            .eq("task_status", TaskStatus.WaitingInDB.code)
                            .le("plan_run_time", now)
                            .orderBy("id")
                            .setMaxRows(1)
                            .findUnique();
                    if (task == null) {
                        return null;
                    }
                    task.task_status = TaskStatus.WaitingInQueue.code;
                    DB.ReadWriteDB().save(task);
                    return task;
                }
            });
            return task;

        } catch (Exception ex) {
            Logger.error(String.format("Failed for LoadTaskFromDb: %s",
                    ExceptionUtil.exceptionChainToString(ex)));
            return null;
        }
    }

    public static void ResetTaskStatus() {
        DB.RunInTransaction(new TxRunnable() {
            @Override
            public void run() {
                String sql = "update `plan_task` set `task_status`=:init_status where `task_status`=:old_status";
                DB.ReadWriteDB().createSqlUpdate(sql)
                        .setParameter("init_status", TaskStatus.WaitingInDB.code)
                        .setParameter("old_status", TaskStatus.WaitingInQueue.code)
                        .execute();
            }
        });
    }
}
