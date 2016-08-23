package K.Controllers.ApiDoc


import K.Aop.annotations.CheckToken
import K.Aop.annotations.Comment
import K.Aop.annotations.JsonApi
import K.Common.BizLogicException
import K.Common.Helper
import K.Common.Token
import K.Common.TokenObject
import K.Controllers.ApiDoc.Reply.ApiInfo
import K.Controllers.JsonpController
import K.Reply.BooleanReply
import play.mvc.Result
import play.mvc.Results

import javax.inject.Inject
import java.lang.reflect.Method

/**
 * Created by kk on 14/10/29.
 */
class ApiDocument
@Inject
constructor(private val definedAPIs: DefinedAPIs) : JsonpController() {

    fun GeneratApiMarkdown(): Result {
        return Results.ok(DocUtils.GeneratApiMarkdown(definedAPIs)).`as`("text/plain")
    }

    fun GeneratApiSample(api_url: String): Result {
        return Results.ok(DocUtils.GeneratApiSample(api_url, definedAPIs)).`as`("text/html")
    }

    fun GeneratApiTestPage(): Result {
        return Results.ok(DocUtils.GeneratApiTestPage(definedAPIs)).`as`("text/html")
    }


    @Comment("根据 token 检查当前 api 是否有授权")
    @CheckToken
    @JsonApi(ReplyClass = BooleanReply::class)
    @Throws(ClassNotFoundException::class)
    fun ApiAuthorization(@Comment("用户登录后的token") token: String,
                         @Comment("需要检测的 API 的 url") api_url: String): Result {
        val apiInfo = this.definedAPIs.getApiInfoByRoute(api_url) ?: throw BizLogicException("Can not find api by url:%s", api_url)

        val reply = BooleanReply()

        val controllerCls = Helper.LoadClass(apiInfo.controller_class)
        val api_method = DocUtils.ApiControllerMethod(controllerCls, apiInfo.method_name)
        val token_anno = api_method!!.getDeclaredAnnotation(CheckToken::class.java)

        if (token_anno == null) {
            // 说明 API 不需要token 就可以访问
            reply.result = true
            return JsonpController.Companion.ok(reply.ToJsonNode())
        }

        val tokenObject = Token.GetTokenObject(token)

        //        if (!token_anno.authorization_check()) {
        //            // 要 token, 但是不做角色的判断
        //            reply.result = true;
        //            return ok(reply.ToJsonNode());
        //        }

        if (token_anno.allowed_roles.size == 0) {
            // 没有指定角色要求
            reply.result = true
            return JsonpController.Companion.ok(reply.ToJsonNode())
        } else {
            reply.result = false
            for (role in token_anno.allowed_roles) {
                if (tokenObject!!.HasRole(role)) {
                    reply.result = true
                    break
                }
            }
        }

        return JsonpController.Companion.ok(reply)
    }

}
