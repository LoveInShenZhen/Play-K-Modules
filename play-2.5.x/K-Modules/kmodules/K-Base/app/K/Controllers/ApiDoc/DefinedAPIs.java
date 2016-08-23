package K.Controllers.ApiDoc;

import K.Aop.annotations.JsonApi;
import K.Common.Hub;
import K.Controllers.ApiDoc.Reply.ApiInfo;
import K.Controllers.ApiDoc.Reply.RouteInfo;
import K.Controllers.ApiDoc.TemplateModel.ApiDefinition;
import K.Controllers.ApiDoc.TemplateModel.ApiGroup;
import play.routing.Router;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by kk on 15/1/2.
 */

public class DefinedAPIs {

    private final Provider<Router> routerProvider;
    private ApiDefinition apiDefinition;

    public DefinedAPIs()
            throws IllegalAccessException,
            InstantiationException,
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException {
        this.routerProvider = Hub.routerProvider();
        apiDefinition = new ApiDefinition();
        RegistApiByRoutes();
    }

    public List<RouteInfo> AllRoutes() {
        List<RouteInfo> routers = new ArrayList<>();
        this.routerProvider.get().documentation().forEach(routeDocumentation ->  {
            RouteInfo routeInfo = new RouteInfo(routeDocumentation.getHttpMethod(),
                    routeDocumentation.getPathPattern(),
                    routeDocumentation.getControllerMethodInvocation());
            routers.add(routeInfo);
        });

        return routers;
    }

    public ApiDefinition getApiDefinition() {
        return apiDefinition;
    }

    public List<ApiInfo> getApiListByGroup(String group_name) {
        ApiGroup group = apiDefinition.GetApiGroupByName(group_name);
        return group.apiInfoList;
    }

    public ApiInfo getApiInfoByRoute(String route_url) {
        Set<String> group_names = groupNames();
        for (String group : group_names) {
            List<ApiInfo> apiInfoList = getApiListByGroup(group);
            for (ApiInfo apiInfo : apiInfoList) {
                if (apiInfo.url.equals(route_url)) {
                    return apiInfo;
                }
            }
        }
        return null;
    }

    public Set<String> groupNames() {
        return apiDefinition.groupNames();
    }

    public void RegistApi(String group_name,
                          String url,
                          String http_method,
                          String controller_class,
                          String method_name,
                          String reply_class)
            throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException {

        List<ApiInfo> apiInfoList = getApiListByGroup(group_name);
        if (apiInfoList.stream().filter(api_info -> api_info.url.equals(url)).count() == 0) {
            // 避免加入重复的
            ApiInfo apiInfo = new ApiInfo(url, http_method, controller_class, method_name, reply_class);
            apiInfoList.add(apiInfo);
        }
    }

    private void RegistApiByRoutes() {
        List<RouteInfo> routeInfoList = AllRoutes();
        try {
            for (RouteInfo routeInfo: routeInfoList) {
                JsonApi api_anno = routeInfo.GetJsonApiAnnotation();
                if (api_anno != null) {
                    RegistApi(routeInfo.ControllerComment(),
                            routeInfo.ApiUrl(),
                            api_anno.ApiMethodType(),
                            routeInfo.ControllerClassName(),
                            routeInfo.ControllerMethodName(),
                            api_anno.ReplyClass().getName()
                    );
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
