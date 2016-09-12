package k.ebean

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 上午10:41
 * To change this template use File | Settings | File Templates.
 */

import com.avaje.ebean.*
import java.util.concurrent.Callable


object DB {

    fun Default(): EbeanServer {
        return Ebean.getServer(null)
    }

    fun <T> RunInTransaction(txCallable: TxCallable<T>): T {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return Ebean.execute(txScope, txCallable)
    }

    fun RunInTransaction(txRunnable: TxRunnable) {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        Ebean.execute(txScope, txRunnable)
    }

    fun RunInTransaction(runnable: Runnable) {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        Ebean.execute(txScope, { runnable })
    }

    fun <T> RunInTransaction(callable: Callable<T>) {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return Ebean.execute(txScope, { callable })
    }

    fun RunInTransaction(body : () -> Unit) {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        Ebean.execute(txScope, body)
    }

    fun <T> RunInTransaction(body: () -> T) : T {
        val txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED)
        return Ebean.execute(txScope, body)
    }

}
