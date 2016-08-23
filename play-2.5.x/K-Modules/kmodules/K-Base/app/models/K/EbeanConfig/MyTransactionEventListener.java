package models.K.EbeanConfig;

import K.Service.NotifyService;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.event.TransactionEventListener;
import models.K.BgTask.PlanTask;
import play.Logger;

/**
 * Created by kk on 15/2/2.
 */
public class MyTransactionEventListener implements TransactionEventListener {

    public MyTransactionEventListener() {
        Logger.debug("Ebean server load MyTransactionEventListener");
    }

    @Override
    public void postTransactionCommit(Transaction tx) {
        if (tx.getUserObject(PlanTask.class.getName()) != null) {
            NotifyService notifyService = NotifyService.getService();
            if (notifyService != null) {
                notifyService.NotifyNewPlanTask();
//                Logger.debug("MyTransactionEventListener::notifyService.NotifyNewPlanTask()");
            }
        }
        Object obj = tx.getUserObject(MyBeanPersistController.class.getName());
        if (obj != null) {
            StringBuilder sb = (StringBuilder) obj;
            Logger.of("PersistLog").info(sb.toString());
        }
    }

    @Override
    public void postTransactionRollback(Transaction tx, Throwable cause) {

    }
}
