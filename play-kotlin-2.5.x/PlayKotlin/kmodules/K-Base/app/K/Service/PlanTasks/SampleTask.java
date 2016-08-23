package K.Service.PlanTasks;

import models.K.BgTask.PlanTask;
import play.Logger;

import java.util.Date;
import java.util.UUID;

/**
 * Created by kk on 14-2-19.
 */
public class SampleTask implements Runnable {

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();
        Logger.debug("==> SampleTask run");
//        throw new RuntimeException("Sample task, 模拟任务异常: " + uuid.toString());
    }

    public static void AddTask() {
        SampleTask task = new SampleTask();
        Date plan_run_time = new Date();// Helper.IncTimeInSec(new Date(), -1);
        Logger.debug("SampleTask plan run time: {}", plan_run_time.getTime());
        PlanTask.Companion.addTask(task, "Sample Test", true, plan_run_time, "hello world");
    }
}
