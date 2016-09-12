package k.common.apidoc


import jodd.util.ReflectUtil
import k.aop.annotations.Comment
import k.aop.annotations.JsonApi

import java.lang.reflect.Method

/**
 * Created by kk on 15/7/20.
 */
class RouteInfo(

        @Comment("API http method: GET or POST")
        private val httpMethod: String,

        @Comment("API url")
        private val url: String,

        @Comment("控制器方法全路径")
        private val controllerPath: String) {

    fun ApiUrl(): String {
        return url
    }

    fun ControllerClass(): Class<*> {
        return RouteInfo::class.java.classLoader.loadClass(this.ControllerClassName())
    }

    fun ControllerClassName(): String {
        val l = controllerPath.lastIndexOf('.')
        return controllerPath.substring(0, l)
    }

    fun ControllerMethodName(): String {
        val l = controllerPath.lastIndexOf('.')
        val r = controllerPath.indexOf('(')
        return controllerPath.substring(l + 1, r)
    }

    fun GetControllerMethod(): Method? {
        val methods = ReflectUtil.getAccessibleMethods(ControllerClass())
        for (method in methods) {
            if (method.name == ControllerMethodName()) {
                return method
            }
        }

        return null
    }

    fun GetJsonApiAnnotation(): JsonApi? {
        val method = GetControllerMethod() ?: return null
        val apiAnno = method.getAnnotation(JsonApi::class.java)
        return apiAnno
    }

    fun ControllerComment(): String {
        val controller_cls = ControllerClass()
        if (controller_cls.getAnnotation(Comment::class.java) != null) {
            return controller_cls.getAnnotation(Comment::class.java).value
        } else {
            return ControllerClassName()
        }
    }


}
