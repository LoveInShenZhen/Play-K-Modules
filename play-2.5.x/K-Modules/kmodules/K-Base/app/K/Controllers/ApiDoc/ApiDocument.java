package K.Controllers.ApiDoc;


import K.Aop.annotations.CheckToken;
import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.BizLogicException;
import K.Common.Helper;
import K.Common.Token;
import K.Common.TokenObject;
import K.Controllers.ApiDoc.Reply.ApiInfo;
import K.Controllers.JsonpController;
import K.Reply.BooleanReply;
import play.mvc.Result;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by kk on 14/10/29.
 */
public class ApiDocument extends JsonpController {

    private DefinedAPIs definedAPIs() {
        try {
            return new DefinedAPIs();
        } catch (Exception ex) {
            throw new BizLogicException(ex.getMessage());
        }
    }

    public Result GeneratApiMarkdown() {
        return ok(DocUtils.GeneratApiMarkdown(definedAPIs())).as("text/plain");
    }

    public Result GeneratApiSample(String api_url) {
        return ok(DocUtils.GeneratApiSample(api_url, definedAPIs())).as("text/html");
    }

    public Result GeneratApiTestPage() {
        return ok(DocUtils.GeneratApiTestPage(definedAPIs())).as("text/html");
    }


    @Comment("根据 token 检查当前 api 是否有授权")
    @CheckToken
    @JsonApi(ReplyClass = BooleanReply.class)
    public Result ApiAuthorization(@Comment("用户登录后的token") String token,
                                   @Comment("需要检测的 API 的 url") String api_url) throws ClassNotFoundException {
        ApiInfo apiInfo = this.definedAPIs().getApiInfoByRoute(api_url);
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
            return ok(reply);
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
            return ok(reply);
        } else {
            reply.result = false;
            for (int role : token_anno.allowed_roles()) {
                if (tokenObject.HasRole(role)) {
                    reply.result = true;
                    break;
                }
            }
        }

        return ok(reply);
    }

}
