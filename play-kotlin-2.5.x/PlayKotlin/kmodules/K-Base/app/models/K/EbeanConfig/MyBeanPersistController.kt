package models.K.EbeanConfig

import K.Aop.annotations.WithPersistLog
import K.Common.Helper
import com.avaje.ebean.event.BeanPersistController
import com.avaje.ebean.event.BeanPersistRequest
import com.google.common.collect.ImmutableSet
import com.google.common.reflect.ClassPath
import play.Logger
import play.libs.Json

import javax.persistence.Entity
import java.util.HashSet

/**
 * Created by kk on 14/11/28.
 */
class MyBeanPersistController : BeanPersistController {

    private val registed_bean_class_names: MutableSet<String>

    init {
        Helper.DLog("Ebean server load MyBeanPersistController")
        registed_bean_class_names = HashSet<String>()
        try {
            val cp = ClassPath.from(MyBeanPersistController::class.java.classLoader)
            val ModelsPackage = "models"
            val classes = cp.getTopLevelClassesRecursive(ModelsPackage)
            for (c in classes) {
                val BeanClass = c.load()
                val entityAnno = BeanClass.getAnnotation(Entity::class.java) ?: continue
                val withPersistLogAnno = BeanClass.getAnnotation(WithPersistLog::class.java) ?: continue
                registed_bean_class_names.add(BeanClass.name)
                //                Logger.debug("MyBeanPersistController regist bean class: {}", BeanClass.getName());
            }
        } catch (ex: Exception) {
            Logger.error(ex.message)
        }

    }

    // When there are multiple BeanPersistController's for a given entity type this controls the order in which they are executed.
    // Lowest values are executed first.
    override fun getExecutionOrder(): Int {
        return 10
    }

    override fun isRegisterFor(cls: Class<*>): Boolean {
        return registed_bean_class_names.contains(cls.name)
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
        WriteLog(request, request.bean, "Insert")
    }

    override fun postUpdate(request: BeanPersistRequest<*>) {
        WriteLog(request, request.bean, "Update")
    }

    override fun postDelete(request: BeanPersistRequest<*>) {
        WriteLog(request, request.bean, "Delete")
    }


    private fun WriteLog(request: BeanPersistRequest<*>, bean: Any, action_type: String) {
        val plog = PersistLog()
        plog.bean_class = bean.javaClass.name
        plog.action_type = action_type
        plog.bean_data = Json.toJson(bean)
        val sb = getBuffer(request)
        sb.append(Helper.ToJsonStringPretty(plog)).append("\n")

    }

    private fun getBuffer(request: BeanPersistRequest<*>): StringBuilder {
        val obj = request.transaction.getUserObject(MyBeanPersistController::class.java.name)
        if (obj == null) {
            val sb = StringBuilder()
            request.transaction.putUserObject(MyBeanPersistController::class.java.name, sb)
            return sb
        } else {
            return obj as StringBuilder
        }
    }
}
