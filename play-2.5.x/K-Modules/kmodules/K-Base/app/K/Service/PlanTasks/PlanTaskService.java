package K.Service.PlanTasks;

import K.Common.Helper;
import K.DataDict.TaskStatus;
import K.Ebean.DB;
import K.Service.ServiceBase;
import com.avaje.ebean.TxRunnable;
import jodd.datetime.JDateTime;
import jodd.exception.ExceptionUtil;
import models.K.BgTask.PlanTask;
import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlanTaskService extends ServiceBase {

    public static final String ServiceName = "PlanTaskService";

    private int task_loader_wait_time = 5;
    private boolean stop_now;

    private ScheduledExecutorService seq_worker;
    private ScheduledExecutorService parallel_worker;

    private Thread seq_task_loader;
    private Thread parallel_task_loader;

    private Object task_notifier;

    private int parallel_worker_thread_count = 4;


    public PlanTaskService() {
        super(ServiceName);
        task_notifier = new Object();
        stop_now = false;
        PlanTask.ResetTaskStatus();
    }

    private void Init() {
        parallel_worker = Executors.newScheduledThreadPool(parallel_worker_thread_count);

        seq_worker = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void Start() {
        if (Running()) {
            return;
        }
        Init();
        stop_now = false;

        start_task_loader(true);
        start_task_loader(false);

        setRunning(true);
        Logger.debug("Plan Task Service Started......");
    }

    @Override
    public boolean Stop() {
        if (!Running()) {
            return true;
        }

        stop_now = true;
        Logger.debug("==> Try to stop plan task loader...");
        NotifyNewTask();

        try {
            parallel_task_loader.join(120000);
            seq_task_loader.join(120000);

            Logger.debug("==> Try to stop plan task worker...");
            parallel_worker.shutdown();
            seq_worker.shutdown();

            parallel_worker.awaitTermination(120, TimeUnit.SECONDS);
            seq_worker.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // todo : 增加错误日志
            Logger.warn(Helper.StackTraceOfEx(e));
            return false;
        }

        setRunning(false);
        Logger.debug("Plan Task Service Stopped......");
        return true;
    }

    private void process_task(final PlanTask task) {
        try {
            if (task != null) {
                final Runnable run_entity = (Runnable) Helper.FromJsonString(task.json_data,
                        Class.forName(task.class_name));

                if (run_entity != null) {
                    try {
                        DB.RunInTransaction(new TxRunnable() {
                            @Override
                            public void run() {
                                run_entity.run();
                                DB.Default().delete(task);
                            }
                        });
                    } catch (Exception ex) {
                        String ex_msg = ExceptionUtil.exceptionChainToString(ex);
                        Logger.error("Run Task: {} Failed.\nException: {}",
                                Helper.ToJsonStringPretty(task),
                                ex_msg);
                        DB.RunInTransaction(new TxRunnable() {
                            @Override
                            public void run() {
                                task.task_status = TaskStatus.Exception.code;
                                task.remarks = ex_msg;
                                DB.Default().save(task);
                            }
                        });

                    }
                }
            }
        } catch (Exception ex) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex));
        }
    }

    private void start_task_loader(boolean require_seq) {
        final List<PlanTask> loaded_tasks = new ArrayList<>();

        final ScheduledExecutorService worker;
        if (require_seq) {
            worker = this.seq_worker;
        } else {
            worker = this.parallel_worker;
        }

        Thread task_loader = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (stop_now) return;

                    try {
                        // Load tasks to parallel_worker
                        DB.RunInTransaction(new TxRunnable() {
                            @Override
                            public void run() {
                                JDateTime end_time = new JDateTime();
                                end_time.addSecond(task_loader_wait_time + 1);

                                List<PlanTask> taskList = PlanTask.find.where()
                                        .eq("require_seq", require_seq)
                                        .eq("task_status", TaskStatus.WaitingInDB.code)
                                        .le("plan_run_time", end_time.convertToDate())
                                        .orderBy("id")
                                        .findList();

                                for (PlanTask task : taskList) {
                                    task.task_status = TaskStatus.WaitingInQueue.code;
                                    DB.Default().save(task);
                                }

                                loaded_tasks.clear();
                                loaded_tasks.addAll(taskList);
                            }
                        });

                        for (PlanTask task : loaded_tasks) {
                            SchedulePlanTask(task, worker);
                        }

                        if (loaded_tasks.size() == 0) {
                            try {
                                synchronized (task_notifier) {
                                    task_notifier.wait(task_loader_wait_time * 1000);
                                }
                            } catch (Exception ex) {
                                // do nothing
                            }
                        }

                    } catch (Exception e) {
                        Logger.error(Helper.StackTraceOfEx(e));
                    }
                }
            }
        });
        task_loader.start();

        if (require_seq) {
            this.seq_task_loader = task_loader;
        } else {
            this.parallel_task_loader = task_loader;
        }
    }

    private void SchedulePlanTask(final PlanTask task, final ScheduledExecutorService woker) {
        JDateTime now = new JDateTime();
        JDateTime plan_run_time = new JDateTime(task.plan_run_time);
        long interval = plan_run_time.getTimeInMillis() - now.getTimeInMillis();
        long delay = interval > 0 ? interval : 0;

        woker.schedule(new Runnable() {
                           @Override
                           public void run() {
                               try {
                                   process_task(task);
                               } catch (Exception ex) {
                                   Logger.error(ExceptionUtil.exceptionChainToString(ex));
                               }
                           }
                       },
                delay,
                TimeUnit.MILLISECONDS);
    }

    public void NotifyNewTask() {
        synchronized (task_notifier) {
            task_notifier.notifyAll();
        }
    }
}
