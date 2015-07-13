package K.Service.PlanTasks.Commons;

import com.fasterxml.jackson.annotation.JsonFormat;
import models.K.BgTask.PlanTask;

import java.util.Date;

/**
 * Created by kk on 14-4-2.
 */
public class RetryUntilTask extends AutoRetryTask {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    public Date stop_time;

    @Override
    protected boolean ContinueRetry() {
        Date now = new Date();
        if (now.before(stop_time)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void addRetryTask(Runnable task) {
        AddRetryUntilTask(task, require_seq, seq_type, delayTimeForRetry, stop_time);
    }

    public static void AddRetryUntilTask(Runnable task, boolean requireSeq, String seqType,
                                         int delayTimeForRetry, Date stopTime) {
        AddRetryUntilTask(task, requireSeq, seqType, delayTimeForRetry, new Date(), stopTime);
    }

    public static void AddRetryUntilTask(Runnable task, boolean requireSeq, String seqType,
                                         int delayTimeForRetry, Date plan_run_time, Date stopTime) {
        AddRetryUntilTask(task, requireSeq, seqType, delayTimeForRetry, plan_run_time, stopTime, "");
    }

    public static void AddRetryUntilTask(Runnable task, boolean requireSeq, String seqType,
                                         int delayTimeForRetry, Date plan_run_time, Date stopTime,
                                         String tag) {
        RetryUntilTask retryTask = new RetryUntilTask();
        retryTask.setWrapperTask(task);
        retryTask.require_seq = requireSeq;
        retryTask.seq_type = seqType;
        retryTask.delayTimeForRetry = delayTimeForRetry;
        retryTask.stop_time = stopTime;

        Date planRunTime = plan_run_time;
        PlanTask.addTask(retryTask, seqType, requireSeq, planRunTime, tag);
    }
}
