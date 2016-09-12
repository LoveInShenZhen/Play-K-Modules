package k.common.apidoc

import play.routing.Router
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by kk on 16/9/12.
 */
@Singleton
class DefinedApis
@Inject private constructor(private val routerProvider: Provider<Router>)  {


    fun AllRoutes(): List<RouteInfo> {
        val routes : MutableList<RouteInfo> = mutableListOf()
        this.routerProvider.get().documentation().forEach {
            val routeInfo = RouteInfo(httpMethod=it.httpMethod,
                    url = it.pathPattern,
                    controllerPath = it.controllerMethodInvocation)

            routes.add(routeInfo)
        }

        return routes
    }

}