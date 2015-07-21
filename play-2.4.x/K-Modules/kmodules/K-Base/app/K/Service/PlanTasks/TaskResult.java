package K.Service.PlanTasks;

import K.Ebean.DB;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxRunnable;
import com.avaje.ebean.TxScope;

/**
 * Created by kk on 14-3-31.
 */
public class TaskResult {
    public boolean OK;
    public Exception ex;

    public TaskResult() {
        OK = false;
        ex = new RuntimeException("Task has not been started.");
    }

    public static void RunTaskInTransaction(final Runnable task, final TaskResult result) {
        try {
            TxScope txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED);
            Ebean.execute(txScope, new TxRunnable() {
                @Override
                public void run() {
                    task.run();
                }
            });
            result.OK = true;
            result.ex = null;
        } catch (Exception ex) {
            result.OK = false;
            result.ex = ex;
        }

    }

    public static void RunTaskWithOutTransaction(final Runnable task, final TaskResult result) {
        try {
            task.run();
            result.OK = true;
            result.ex = null;
        } catch (Exception ex) {
            result.OK = false;
            result.ex = ex;
        }
    }
}
