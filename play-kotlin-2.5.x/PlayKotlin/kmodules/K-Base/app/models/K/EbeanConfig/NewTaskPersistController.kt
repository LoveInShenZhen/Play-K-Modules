package models.K.EbeanConfig


import K.Common.Helper
import com.avaje.ebean.event.BeanPersistController
import com.avaje.ebean.event.BeanPersistRequest
import models.K.BgTask.PlanTask

/**
 * Created by kk on 15/1/20.
 */
class NewTaskPersistController : BeanPersistController {

    init {
        Helper.DLog("Ebean server load NewTaskPersistController")
    }

    override fun getExecutionOrder(): Int {
        return 11
    }

    override fun isRegisterFor(cls: Class<*>): Boolean {
        val bean_cls_name = cls.name
        if (bean_cls_name == PlanTask::class.java.name) {
            return true
        }
        return false
    }

    override fun preInsert(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun preUpdate(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun preDelete(request: BeanPersistRequest<*>): Boolean {
        return true
    }

    override fun postInsert(request: BeanPersistRequest<*>) {
        val bean_cls_name = request.bean.javaClass.name
        //        Logger.debug("New Task: {}", Helper.ToJsonStringPretty(request.getBean()));
        if (bean_cls_name == PlanTask::class.java.name) {
            request.transaction.putUserObject(PlanTask::class.java.name, "")
        }
    }

    override fun postUpdate(request: BeanPersistRequest<*>) {
        // do nothing
    }

    override fun postDelete(request: BeanPersistRequest<*>) {
        // do nothing
    }

}
