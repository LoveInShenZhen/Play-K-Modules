package K.Service.PlanTasks.Commons.CronTask;

import K.Common.BizLogicException;
import models.K.BgTask.PlanTask;
import play.libs.Time;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by kk on 15/7/16.
 */
public abstract class CronTaskBase implements Runnable {

    public String cron;

    @Override
    public void run() {
        DoTask();
        AddTask();
    }

    public abstract void DoTask();

    public abstract String Tag();

    public abstract boolean RequireSeq();

    public Date NextPlanRunTime() {
        try {
            Time.CronExpression cronexp = new Time.CronExpression(this.cron);
            return cronexp.getNextValidTimeAfter(new Date());
        } catch (ParseException e) {
            throw new BizLogicException("错误的 Cron 表达式: ", this.cron);
        }
    }

    public void AddTask() {
        PlanTask.Companion.addSingletonTask(this,
                "CronTask",
                RequireSeq(),
                NextPlanRunTime(),
                Tag()
        );
    }

}
