package k.task

import jodd.datetime.JDateTime
import jodd.exception.ExceptionUtil
import k.common.AnsiColor
import k.common.Helper
import k.common.Hub
import k.ebean.DB
import models.task.PlanTask
import models.task.TaskStatus
import play.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Created by kk on 16/9/19.
 */
object PlanTaskService {

    private val seqWorker: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val parallelWorker: ScheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())

    private var seqTaskLoader: Thread? = null
    private var parallerTaskLoader: Thread? = null
    private val taskNotifier = Object()
    private val taskLoaderWaitTime = 5
    private var stopNow: Boolean = true

    var isRunning: Boolean = false
        private set

    fun Start() {
        if (isRunning) return

        stopNow = false

        seqTaskLoader = BuildTaskLoader(true, seqWorker)
        parallerTaskLoader = BuildTaskLoader(false, parallelWorker)

        seqTaskLoader!!.start()
        parallerTaskLoader!!.start()

        isRunning = true
        Helper.DLog(AnsiColor.GREEN, "Plan Task Service Started......")
    }

    fun Stop() {
        if (!isRunning) return
        stopNow = true
        try {
            Helper.DLog(AnsiColor.GREEN, "Try to stop plan task loader...")
            NotifyNewTask()
            seqTaskLoader!!.join(120000)
            parallerTaskLoader!!.join(120000)

            Helper.DLog(AnsiColor.GREEN, "Try to stop plan task worker...")
            seqWorker.shutdown()
            parallelWorker.shutdown()

            seqWorker.awaitTermination(120, TimeUnit.SECONDS)
            parallelWorker.awaitTermination(120, TimeUnit.SECONDS)

            Helper.DLog(AnsiColor.GREEN, "Plan Task Service Stopped......")
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex))
        } finally {
            isRunning = false
        }
    }

    private fun BuildTaskLoader(requireSeq: Boolean, worker: ScheduledExecutorService): Thread {
        return Thread(Runnable {
            val loadedTasks = mutableListOf<PlanTask>()
            while (true) {
                if (stopNow) return@Runnable

                try {
                    DB.RunInTransaction {
                        val endTime = JDateTime().addSecond(taskLoaderWaitTime + 1)
                        val tasks = PlanTask.where()
                                .eq("require_seq", requireSeq)
                                .eq("task_status", TaskStatus.WaitingInDB.code)
                                .le("plan_run_time", endTime)
                                .findList()

                        tasks.forEach {
                            it.task_status = TaskStatus.WaitingInQueue.code
                        }
                        DB.Default().saveAll(tasks)

                        loadedTasks.clear()
                        loadedTasks.addAll(tasks)
                    }   // 事务截至点

                    if (loadedTasks.size > 0) {
                        loadedTasks.forEach {
                            SchedulePlanTask(it, worker)
                        }
                    } else {
                        // 在 endTime 之前没有需要执行的 task, 尝试等待新任务, 释放 cpu
                        try {
                            synchronized(taskNotifier, {
                                taskNotifier.wait(taskLoaderWaitTime * 1000L)
                            })
                        } catch (ex: Exception) {
                            // do nothing
                        }
                    }

                } catch (ex: Exception) {
                    loadedTasks.clear()
                    Logger.error(ExceptionUtil.exceptionChainToString(ex))
                }
            }
        })
    }

    private fun SchedulePlanTask(task: PlanTask, worker: ScheduledExecutorService) {
        val now = JDateTime()
        if (task.plan_run_time == null) task.plan_run_time = now
        val interval = task.plan_run_time!!.timeInMillis - now.timeInMillis
        val delay = if (interval > 0) interval else 0

        worker.schedule({
            try {
                process_task(task)
            } catch (ex: Exception) {
                Logger.error(ExceptionUtil.exceptionChainToString(ex))
            }
        },
                delay,
                TimeUnit.MILLISECONDS)
    }

    private fun DeserializeJsonData(task: PlanTask): Runnable? {
        try {
            return Helper.FromJsonString(task.json_data!!, Class.forName(task.class_name)) as Runnable
        } catch (ex: Exception) {
            return null
        }
    }

    private fun process_task(task: PlanTask) {
        try {
            val runObj = DeserializeJsonData(task)
            if (runObj != null) {
                try {
                    DB.RunInTransaction({
                        runObj.run()    // 执行任务
                        task.refresh()
                        task.delete()   // 任务执行成功后, 从数据库里删除记录
                    })
                } catch (ex: Exception) {
                    // 任务执行发生错误, 标记任务状态, 记录
                    DB.RunInTransaction {
                        task.refresh()
                        task.task_status = TaskStatus.Error.code
                        task.remarks = ExceptionUtil.exceptionChainToString(ex)
                        task.save()
                    }
                }
            } else {
                DB.RunInTransaction {
                    task.refresh()
                    task.task_status = TaskStatus.Error.code
                    task.remarks = "反序列化任务失败"
                    task.save()
                }
            }
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex))
        }
    }

    fun NotifyNewTask() {
        synchronized(taskNotifier, {
            taskNotifier.notifyAll()
        })
    }

    fun Enabled(): Boolean {
        return Hub.configuration().getBoolean("k.planTaskService", false)
    }
}