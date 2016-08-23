package models.K.EbeanConfig

import K.Common.Helper
import K.Service.NotifyService
import com.avaje.ebean.Transaction
import com.avaje.ebean.event.TransactionEventListener
import models.K.BgTask.PlanTask
import play.Logger

/**
 * Created by kk on 15/2/2.
 */
class MyTransactionEventListener : TransactionEventListener {

    init {
        Helper.DLog("Ebean server load MyTransactionEventListener")
    }

    override fun postTransactionCommit(tx: Transaction) {
        if (tx.getUserObject(PlanTask::class.java.name) != null) {
            val notifyService = NotifyService.getService()
            notifyService?.NotifyNewPlanTask()
        }
        val obj = tx.getUserObject(MyBeanPersistController::class.java.name)
        if (obj != null) {
            val sb = obj as StringBuilder
            Logger.of("PersistLog").info(sb.toString())
        }
    }

    override fun postTransactionRollback(tx: Transaction, cause: Throwable) {

    }
}
