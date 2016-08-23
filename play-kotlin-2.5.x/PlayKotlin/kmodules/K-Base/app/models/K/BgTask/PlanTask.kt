package models.K.BgTask

/**
 * Created by kk on 14-1-21.
 */

import K.Common.Helper
import K.DataDict.TaskStatus
import K.Ebean.DB
import com.avaje.ebean.Model
import com.fasterxml.jackson.annotation.JsonFormat
import play.data.format.Formats
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "plan_task")
class PlanTask : Model() {

    @Id
    var id: Long = 0

    @Column(columnDefinition = "TINYINT(1) DEFAULT '1' COMMENT '是否要求顺序执行'")
    var require_seq: Boolean = false

    @Column(columnDefinition = "VARCHAR(64) DEFAULT 'global_seq' COMMENT '顺序执行的类别'", nullable = false)
    var seq_type: String? = null

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "DATETIME COMMENT '任务创建时间'", nullable = false)
    var create_time: Date? = null

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'", nullable = false)
    var plan_run_time: Date? = null

    @Column(columnDefinition = "INTEGER DEFAULT 0 COMMENT '任务状态: 0:WaitingInDB, 7:WaitingInQueue, 8:Exception'", nullable = false)
    var task_status: Int = 0

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Runable task class name'", nullable = false)
    var class_name: String? = null

    @Column(columnDefinition = "TEXT COMMENT 'Runable task class json data'", nullable = false)
    var json_data: String? = null

    @Column(columnDefinition = "TEXT COMMENT '标签,用于保存任务相关的额外数据'")
    var tag: String? = null

    @Column(columnDefinition = "TEXT COMMENT '发生异常情况的时候, 用于记录额外信息'")
    var remarks: String? = null

    companion object {

        var find = Model.Finder<Long, PlanTask>(PlanTask::class.java)

        @JvmOverloads fun addTask(obj: Any, seqType: String, requireSeq: Boolean, planRunTime: Date, tag: String = "") {
            val now = Date()
            val planTask = PlanTask()
            planTask.require_seq = requireSeq
            planTask.seq_type = seqType
            planTask.create_time = Helper.ZeroMillisecond(now)
            planTask.plan_run_time = Helper.ZeroMillisecond(planRunTime)
            planTask.task_status = TaskStatus.WaitingInDB.code
            planTask.class_name = obj.javaClass.name
            planTask.json_data = Helper.ToJsonString(obj)
            planTask.tag = tag

            DB.Default().save(planTask)
        }

        fun addTask(obj: Any, seqType: String, requireSeq: Boolean) {
            val now = Date()
            addTask(obj, seqType, requireSeq, now)
        }

        fun addSingletonTask(obj: Any, seqType: String, requireSeq: Boolean, planRunTime: Date, tag: String) {
            val class_name = obj.javaClass.name
            val task = PlanTask.find.where()
                    .eq("class_name", class_name)
                    .eq("task_status", TaskStatus.WaitingInDB.code)
                    .findUnique()
            if (task == null) {
                addTask(obj, seqType, requireSeq, planRunTime, tag)
            } else {
                task.plan_run_time = planRunTime
                DB.Default().save(task)
            }
        }

        fun ResetTaskStatus() {
            DB.RunInTransaction {
                val sql = "update `plan_task` set `task_status`=:init_status where `task_status`=:old_status"
                DB.Default().createSqlUpdate(sql)
                        .setParameter("init_status", TaskStatus.WaitingInDB.code)
                        .setParameter("old_status", TaskStatus.WaitingInQueue.code)
                        .execute()
            }
        }
    }
}
