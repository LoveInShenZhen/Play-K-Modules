package K.Service;

import K.Service.PlanTasks.PlanTaskService;
import jodd.http.HttpRequest;
import play.Configuration;
import play.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 14-1-15.
 */
public class NotifyService extends ServiceBase {
    public static String ServiceName = "NotifyService";

    private ExecutorService notifier;

    private String PlanTaskNotifyUrl;

    private Configuration configuration;

    public NotifyService(Configuration configuration) {
        super(ServiceName);
        this.configuration = configuration;
    }

    public static NotifyService getService() {
        NotifyService service = (NotifyService) ServiceMgr.Instance.getService(ServiceName);
        return service;
    }

    private void LoadConf() {
        PlanTaskNotifyUrl = this.configuration.getString("K.plan_task_notify_url",
                "http://127.0.0.1:9000/sys/notify/NewPlanTask");
    }

    private void Init() {
        LoadConf();
        notifier = Executors.newCachedThreadPool();
    }

    @Override
    public void Start() {
        if (Running()) {
            return;
        }

        Init();
        setRunning(true);
        Logger.debug("==>     NotifyService Started......");
    }

    @Override
    public boolean Stop() {
        if (!Running()) {
            return true;
        }

        try {
            notifier.shutdown();
            notifier.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // todo : 增加错误日志
            e.printStackTrace();
            return false;
        }
        setRunning(false);
        Logger.debug("==>     NotifyService Stopped......");
        return true;
    }

    public void NotifyNewPlanTask() {
        PlanTaskService planTaskService = (PlanTaskService) ServiceMgr.Instance.getService(PlanTaskService.ServiceName);
        if (planTaskService != null) {
            planTaskService.NotifyNewTask();
        } else {
            notifier.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpRequest.get(PlanTaskNotifyUrl).timeout(1000).send();
                    } catch (Exception ex) {
                        // todo: add log here
                        return;
                    }
                }
            });
        }
    }

}
