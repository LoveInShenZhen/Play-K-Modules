package controllers


import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.apidoc.DefinedApis
import k.common.apidoc.FieldSchema
import k.common.json.JsonDataType
import k.controllers.JsonpController
import k.reply.StringReply
import k.reply.sample.SampleReply
import play.mvc.Result
import javax.inject.Inject

/**
 * Created by kk on 16/8/23.
 */

@Comment("测试组方法")
class Sample
@Inject
constructor(var definedApis: DefinedApis) : JsonpController() {


    @Comment("测试方法1")
    @JsonApi(ReplyClass = SampleReply::class)
    fun kktest() : Result {
        val reply = SampleReply()
        val replySchema = FieldSchema()
        replySchema.name = "root"
        replySchema.desc = "对象根节点"
        replySchema.type = JsonDataType.OBJECT.typeName

        FieldSchema.resolveFields(SampleReply::class, replySchema)

        return ok(reply)
    }

    @Comment("测试方法2")
    @JsonApi(ReplyClass = StringReply::class)
    fun kktest2(msg: String) : Result {
        val reply = StringReply()
        reply.result = "Received: $msg"
        return ok(reply)
    }

}