package K.Service.PlanTasks;

import K.DB;
import K.DataDict.TaskStatus;
import K.Helper;
import K.Service.ServiceBase;
import com.avaje.ebean.TxRunnable;
import jodd.exception.ExceptionUtil;

import models.K.BgTask.PlanTask;
import play.Logger;


import java.util.concurrent.*;

public class PlanTaskService extends ServiceBase {

    public static final String ServiceName = "PlanTaskService";

    private int task_loader_wait_time = 1;
    private boolean stop_now;

    private ExecutorService seq_worker;
    private ExecutorService parallel_worker;

    private BlockingQueue<PlanTask> seq_task_queue;
    private BlockingQueue<PlanTask> parallel_task_queue;

    private Thread seq_task_loader;
    private Thread parallel_task_loader;

    private Object seq_task_notifier;
    private Object parallel_task_notifier;

    private int parallel_worker_thread_count = 4;


    public PlanTaskService() {
        super(ServiceName);
        seq_task_notifier = new Object();
        parallel_task_notifier = new Object();
        stop_now = false;
        PlanTask.ResetTaskStatus();
    }

    private void Init() {
        parallel_task_queue = new ArrayBlockingQueue<>(parallel_worker_thread_count * 2);
        parallel_worker = Executors.newCachedThreadPool();

        seq_task_queue = new ArrayBlockingQueue<>(2);
        seq_worker = Executors.newSingleThreadExecutor();
    }

    @Override
    public void Start() {
        if (Running()) {
            return;
        }
        Init();
        stop_now = false;

        start_seq_task_loader();
        start_seq_worker();

        start_parallel_task_loader();
        start_parallel_worker();

        setRunning(true);
        Logger.debug("Plan Task Service Started......");
    }

    @Override
    public boolean Stop() {
        if (!Running()) {
            return true;
        }

        stop_now = true;
        NotifyNewTask();

        try {
            parallel_task_loader.join(120000);
            seq_task_loader.join(120000);

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

    private void start_seq_task_loader() {
        seq_task_loader = new Thread(new Runnable() {
            @Override
            public void run() {
                PlanTask task = null;
                while (true) {
                    if (stop_now) return;

                    try {
                        if (task == null) {
                            // 从DB 里加载一个任务, 同时, 任务也从数据库里删除掉了
                            task = PlanTask.LoadTaskFromDb(true);
                        }

                        if (task == null) {
                            // 没有任务, 等通知
                            synchronized (seq_task_notifier) {
                                seq_task_notifier.wait(task_loader_wait_time * 1000);
                            }
                            continue;
                        }

                        // 有任务, 刚从 DB 里加载的, 或者是上次没有成功加入队列的
                        // 再一次试图加入到队列里
                        boolean ret = seq_task_queue.offer(task, 1, TimeUnit.SECONDS);
                        if (ret) {
                            // 成功加入到队列, 设置 task=null, 则下一次的循环就可以再次从
                            // DB 里加载任务了
                            task = null;
                        }

                    } catch (Exception ex) {
                        Logger.error(Helper.StackTraceOfEx(ex));
                    }

                }
            }
        });

        seq_task_loader.start();
    }

    private void start_seq_worker() {
        seq_worker.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (stop_now && seq_task_queue.isEmpty()) {
                        return;
                    }
                    try {
                        PlanTask task = seq_task_queue.poll(1, TimeUnit.SECONDS);
                        process_task(task);
                    } catch (Exception ex) {
                        Logger.error(ExceptionUtil.exceptionChainToString(ex));
                    }
                }
            }
        });
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
                                DB.ReadWriteDB().delete(task);
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
                                DB.ReadWriteDB().save(task);
                            }
                        });

                    }
                }
            }
        } catch (Exception ex) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex));
        }
    }

    private void start_parallel_task_loader() {
        parallel_task_loader = new Thread(new Runnable() {
            @Override
            public void run() {
                PlanTask task = null;
                while (true) {
                    if (stop_now) return;

                    try {
                        if (task == null) {
                            // 从DB 里加载一个任务, 同时, 任务也从数据库里删除掉了
                            task = PlanTask.LoadTaskFromDb(false);
                        }

                        if (task == null) {
                            // 没有任务, 等通知
                            synchronized (parallel_task_notifier) {
                                parallel_task_notifier.wait(task_loader_wait_time * 1000);
                            }

                            continue;
                        }
                        // 有任务, 刚从 DB 里加载的, 或者是上次没有成功加入队列的
                        // 再一次试图加入到队列里
                        boolean ret = parallel_task_queue.offer(task, 1, TimeUnit.SECONDS);
                        if (ret) {
                            // 成功加入到队列, 设置 task=null, 则下一次的循环就可以再次从
                            // DB 里加载任务了
                            task = null;
                        }
                    } catch (Exception e) {
                        Logger.error(Helper.StackTraceOfEx(e));
                    }
                }
            }
        });
        parallel_task_loader.start();
    }

    private void start_parallel_worker() {
        for (int i = 0; i < parallel_worker_thread_count; ++i) {
            parallel_worker.submit(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (stop_now && parallel_task_queue.isEmpty()) {
                            return;
                        }

                        try {
                            PlanTask task = parallel_task_queue.poll(1, TimeUnit.SECONDS);
                            process_task(task);
                        } catch (Exception ex) {
                            Logger.error(ExceptionUtil.exceptionChainToString(ex));
                        }
                    }
                }
            });
        }
    }

    public void NotifyNewTask() {
//        Logger.debug("==> someone NotifyNewTask");
        synchronized (seq_task_notifier) {
            seq_task_notifier.notifyAll();
        }
        synchronized (parallel_task_notifier) {
            parallel_task_notifier.notifyAll();
        }
    }
}
