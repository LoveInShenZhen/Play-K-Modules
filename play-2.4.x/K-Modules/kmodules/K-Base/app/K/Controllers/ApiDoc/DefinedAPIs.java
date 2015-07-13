package K.Controllers.ApiDoc;

import K.Reply.BooleanReply;
import K.Controllers.ApiDoc.Reply.ApiInfo;
import K.Controllers.ApiDoc.TemplateModel.ApiDefinition;
import K.Controllers.ApiDoc.TemplateModel.ApiGroup;

import jodd.exception.ExceptionUtil;
import play.Logger;


import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * Created by kk on 15/1/2.
 */
public class DefinedAPIs {

    private static DefinedAPIs instance;

    static {
        try {
            instance = new DefinedAPIs();
        } catch (Exception e) {
            Logger.error(ExceptionUtil.exceptionChainToString(e));
        }
    }

    private ApiDefinition apiDefinition;

    public DefinedAPIs() throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        apiDefinition = new ApiDefinition();
        RegistAPIs();
    }

    public static DefinedAPIs Instance() {
        return instance;
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

    private void RegistApi(String group_name,
                           String url,
                           String http_method,
                           String controller_class,
                           String method_name,
                           String reply_class)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        List<ApiInfo> apiInfoList = getApiListByGroup(group_name);
        ApiInfo apiInfo = new ApiInfo(url, http_method, controller_class, method_name, reply_class);
        apiInfoList.add(apiInfo);
    }

    private void RegistAPIs() throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException {

        //<editor-fold desc="测试工具接口">
//        RegistApi("测试工具接口",
//                "/api/sample/Test",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "Test",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/sample/kktest",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "kktest",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/test/TriggerRepayment",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "TriggerRepayment",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/test/TriggerP2PReapyment",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "TriggerP2PReapyment",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/sample/TokenInfo",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "TokenInfo",
//                JsonNodeReply.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/sample/TestSendEmail",
//                "GET",
//                controllers.Sample.Sample.class.getName(),
//                "TestSendEmail",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/sample/TestPostJson",
//                "POST JSON",
//                controllers.Sample.Sample.class.getName(),
//                "TestPostJson",
//                ReplyBase.class.getName()
//        );
//
//        RegistApi("测试工具接口",
//                "/api/sample/TestPostForm",
//                "POST FORM",
//                controllers.Sample.Sample.class.getName(),
//                "TestPostForm",
//                ReplyBase.class.getName()
//        );
//        //</editor-fold>

        //<editor-fold desc="API 权限检查判断">
        RegistApi("API 权限检查判断",
                "/api/ApiAuthorization",
                "GET",
                K.Controllers.ApiDoc.Document.class.getName(),
                "ApiAuthorization",
                BooleanReply.class.getName()
        );
        //</editor-fold>
    }
}
