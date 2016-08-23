package K.Controllers.ApiDoc

import K.Aop.annotations.Comment
import K.Aop.annotations.JsonApi
import K.Common.Helper
import K.Controllers.ApiDoc.Reply.ApiInfo
import K.Controllers.ApiDoc.SampleForm.PostFormSample
import K.Controllers.ApiDoc.SampleForm.PostJsonSample
import K.Controllers.JsonpController
import K.Reply.ReplyBase
import K.Reply.StringReply
import com.fasterxml.jackson.databind.JsonNode
import play.Logger
import play.data.Form
import play.libs.Json
import play.mvc.Result

/**
 * Created by kk on 15/7/17.
 */

@Comment("临时测试用, 样例")
class ApiSample : JsonpController() {

    @Comment("Sample, 测试 Post Json 数据")
    @JsonApi(ReplyClass = ReplyBase::class, ApiMethodType = ApiInfo.PostJson, PostDataClass = PostJsonSample::class)
    fun TestPostJson(@Comment("回显信息") echo_msg: String): Result {
        val reply = StringReply()
        val json = request().body().asJson()
        Logger.debug("body as text: {}", request().body().asText())
        val post_data = Json.fromJson(json, PostJsonSample::class.java)
        Logger.debug("Post json data: {}", Helper.ToJsonStringPretty(post_data))
        return JsonpController.Companion.ok(reply.ToJsonNode())
    }

    @Comment("Sample, 测试 Post Form 数据")
    @JsonApi(ReplyClass = ReplyBase::class, ApiMethodType = ApiInfo.PostForm, PostDataClass = PostFormSample::class)
    fun TestPostForm(@Comment("回显信息") echo_msg: String): Result {
        val reply = ReplyBase()
        Logger.debug("Content Type: {}", request().getHeader("Content-Type"))

        val borrowerEditForm = PostFormSample.theForm.bindFromRequest()

        Logger.debug("Post form data: {}", Helper.ToJsonStringPretty(borrowerEditForm.get()))
        return JsonpController.Companion.ok(reply.ToJsonNode())
    }

}
