package K.Controllers.ApiDoc

import K.Aop.annotations.JsonApi
import K.Controllers.ApiDoc.Reply.ApiInfo
import K.Controllers.ApiDoc.Reply.RouteInfo
import K.Controllers.ApiDoc.TemplateModel.ApiDefinition
import K.Controllers.ApiDoc.TemplateModel.ApiGroup
import play.routing.Router

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import java.lang.reflect.InvocationTargetException
import java.util.ArrayList
import kotlin.reflect.jvm.jvmName

/**
 * Created by kk on 15/1/2.
 */

@Singleton
class DefinedAPIs
@Inject
@Throws(IllegalAccessException::class, InstantiationException::class, ClassNotFoundException::class, NoSuchMethodException::class, InvocationTargetException::class)
private constructor(private val routerProvider: Provider<Router>) {
    val apiDefinition: ApiDefinition

    init {
        apiDefinition = ApiDefinition()
        RegistApiByRoutes()
    }

    fun AllRoutes(): List<RouteInfo> {
        val routers = ArrayList<RouteInfo>()
        this.routerProvider.get().documentation().forEach { routeDocumentation ->
            val routeInfo = RouteInfo(routeDocumentation.httpMethod,
                    routeDocumentation.pathPattern,
                    routeDocumentation.controllerMethodInvocation)
            routers.add(routeInfo)
        }

        return routers
    }

    fun getApiListByGroup(group_name: String): MutableList<ApiInfo> {
        val group = apiDefinition.GetApiGroupByName(group_name)
        return group.apiInfoList
    }

    fun getApiInfoByRoute(route_url: String): ApiInfo? {
        val group_names = groupNames()
        for (group in group_names) {
            val apiInfoList = getApiListByGroup(group)
            for (apiInfo in apiInfoList) {
                if (apiInfo.url == route_url) {
                    return apiInfo
                }
            }
        }
        return null
    }

    fun groupNames(): Set<String> {
        return apiDefinition.groupNames()
    }

    @Throws(InstantiationException::class, IllegalAccessException::class, ClassNotFoundException::class, NoSuchMethodException::class, InvocationTargetException::class)
    fun RegistApi(group_name: String,
                  url: String,
                  http_method: String,
                  controller_class: String,
                  method_name: String,
                  reply_class: String) {

        val apiInfoList = getApiListByGroup(group_name)
        if (apiInfoList.filter({ api_info -> api_info.url == url }).count() == 0) {
            // 避免加入重复的
            val apiInfo = ApiInfo(url, http_method, controller_class, method_name, reply_class)
            apiInfoList.add(apiInfo)
        }
    }

    private fun RegistApiByRoutes() {
        val routeInfoList = AllRoutes()
        try {
            for (routeInfo in routeInfoList) {
                val api_anno = routeInfo.GetJsonApiAnnotation()
                if (api_anno != null) {
                    RegistApi(routeInfo.ControllerComment(),
                            routeInfo.ApiUrl(),
                            api_anno.ApiMethodType,
                            routeInfo.ControllerClassName(),
                            routeInfo.ControllerMethodName(),
                            api_anno.ReplyClass.jvmName)
                }
            }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }
}
