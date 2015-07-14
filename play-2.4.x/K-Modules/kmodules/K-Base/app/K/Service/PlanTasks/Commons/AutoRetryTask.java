package K.Service.PlanTasks.Commons;

import K.Common.Helper;
import K.Service.PlanTasks.TaskResult;
import models.K.BgTask.PlanTask;
import play.Logger;


import java.util.Date;

/**
 * Created by kk on 14-2-19.
 */
public class AutoRetryTask implements Runnable {

    public String taskClassName;
    public String taskJsonData;
    public int delayTimeForRetry;
    public int retryTimes;
    public boolean require_seq;
    public String seq_type;

    public AutoRetryTask() {
        delayTimeForRetry = 60; // 60 seconds
        require_seq = true;
        seq_type = "trading";
        retryTimes = 0;
    }

    public void setWrapperTask(Runnable task) {
        taskClassName = task.getClass().getName();
        taskJsonData = Helper.ToJsonString(task);
    }

    protected boolean ContinueRetry() {
        return true;
    }

    @Override
    public void run() {
        Runnable task;

        try {
            task = (Runnable) Helper.FromJsonString(taskJsonData, Class.forName(taskClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (task == null) {
            throw new RuntimeException(String.format("Failed convert json: %s \nto %s",
                    taskJsonData, taskClassName));
        }

        TaskResult result = new TaskResult();
        TaskResult.RunTaskInTransaction(task, result);
        if (!result.OK) {
            // 任务执行失败
            Logger.warn("Run a {} failed. {}\n{}",
                    taskClassName,
                    Helper.ToJsonStringPretty(this),
                    Helper.StackTraceOfEx(result.ex));
            if (ContinueRetry()) {
                addRetryTask(task);
            }
        }
    }

    protected void addRetryTask(Runnable task) {
        AddAutoRetryTask(task, require_seq, seq_type, delayTimeForRetry);
    }

    public static void AddAutoRetryTask(Runnable task, boolean requireSeq, String seqType,
                                        int delayTimeForRetry, String tag) {
        AutoRetryTask retryTask = new AutoRetryTask();
        retryTask.setWrapperTask(task);
        retryTask.require_seq = requireSeq;
        retryTask.seq_type = seqType;
        retryTask.delayTimeForRetry = delayTimeForRetry;

        Date planRunTime = Helper.IncTimeInSec(new Date(), delayTimeForRetry);
        PlanTask.addTask(retryTask, seqType, requireSeq, planRunTime, tag);
    }

    public static void AddAutoRetryTask(Runnable task, boolean requireSeq, String seqType, int delayTimeForRetry) {
        AddAutoRetryTask(task, requireSeq, seqType, delayTimeForRetry, "");
    }

}
