package controllers


import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.Helper
import k.common.apidoc.FieldSchema
import k.common.json.JsonDataType
import k.controllers.JsonpController
import k.reply.sample.SampleReply
import play.mvc.Result

/**
 * Created by kk on 16/8/23.
 */

@Comment("测试组方法")
class Sample : JsonpController() {


    @Comment("测试方法1")
    @JsonApi(ReplyClass = SampleReply::class)
    fun kktest() : Result {
        val reply = SampleReply()
        val replySchema = FieldSchema()
        replySchema.name = "root"
        replySchema.desc = "对象根节点"
        replySchema.jsonType = JsonDataType.OBJECT.typeName

        FieldSchema.resolveFields(SampleReply::class, replySchema)

        Helper.DLog(Helper.ToJsonStringPretty(replySchema))

        return ok(reply)
    }

}