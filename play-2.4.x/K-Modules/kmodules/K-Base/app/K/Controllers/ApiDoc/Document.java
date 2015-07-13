package K.Controllers.ApiDoc;


import K.BizLogicException;
import K.Helper;
import K.Reply.BooleanReply;
import K.Template.FileTemplateHelper;
import K.Token;
import K.TokenObject;
import K.Aop.annotations.CheckToken;
import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Controllers.ApiDoc.Reply.ApiInfo;
import K.Controllers.JsonpController;
import freemarker.template.TemplateException;

import play.mvc.Result;


import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by kk on 14/10/29.
 */
public class Document extends JsonpController {

    public Result GeneratApiMarkdown()
            throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException, TemplateException {
        DefinedAPIs apIs = DefinedAPIs.Instance();
        String md = FileTemplateHelper.Process("/ApiDoc", apIs.getApiDefinition());
        return ok(md);
    }

    public Result GeneratApiSample(String api_url) throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException, TemplateException {
        DefinedAPIs apIs = DefinedAPIs.Instance();
        ApiInfo apiInfo = apIs.getApiInfoByRoute(api_url);
        if (apiInfo == null) {
            return ok("<p>Can not find api by url</p>");
        }
        String html = FileTemplateHelper.Process("/ApiSample.html", apiInfo);
        return ok(html).as("text/html");
    }

    public Result GeneratApiTestPage() throws IOException, TemplateException {
        DefinedAPIs apIs = DefinedAPIs.Instance();
        String html = FileTemplateHelper.Process("/ApiTest.html", apIs.getApiDefinition());
        return ok(html).as("text/html");
    }

    @Comment("根据 token 检查当前 api 是否有授权")
    @CheckToken
    @JsonApi
    public Result ApiAuthorization(@Comment("用户登录后的token") String token,
                                          @Comment("需要检测的 API 的 url") String api_url) throws ClassNotFoundException {
        DefinedAPIs apIs = DefinedAPIs.Instance();
        ApiInfo apiInfo = apIs.getApiInfoByRoute(api_url);
        if (apiInfo == null) {
            throw new BizLogicException("Can not find api by url:%s", api_url);
        }

        BooleanReply reply = new BooleanReply();

        Class controllerCls = Helper.LoadClass(apiInfo.controller_class);
        Method api_method = DocUtils.ApiControllerMethod(controllerCls, apiInfo.method_name);
        CheckToken token_anno = api_method.getDeclaredAnnotation(CheckToken.class);

        if (token_anno == null) {
            // 说明 API 不需要token 就可以访问
            reply.result = true;
            return ok(reply.ToJsonNode());
        }

        TokenObject tokenObject = Token.GetTokenObject(token);

//        if (!token_anno.authorization_check()) {
//            // 要 token, 但是不做角色的判断
//            reply.result = true;
//            return ok(reply.ToJsonNode());
//        }

        if (token_anno.allowed_roles().length == 0) {
            // 没有指定角色要求
            reply.result = true;
            return ok(reply.ToJsonNode());
        } else {
            reply.result = false;
            for (int role : token_anno.allowed_roles()) {
                if (tokenObject.HasRole(role)) {
                    reply.result = true;
                    break;
                }
            }
        }

        return ok(reply.ToJsonNode());
    }

}
