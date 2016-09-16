package controllers


import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.Helper
import k.common.apidoc.DefinedApis
import k.common.apidoc.FieldSchema
import k.common.json.JsonDataType
import k.controllers.JsonpController
import k.reply.StringReply
import k.reply.sample.SampleReply
import org.apache.commons.io.IOUtils
import play.mvc.Result
import java.nio.charset.Charset
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
        replySchema.jsonType = JsonDataType.OBJECT.typeName

        FieldSchema.resolveFields(SampleReply::class, replySchema)

        this.definedApis.AllRoutes().forEach {
            Helper.DLog(it.ControllerComment())
            Helper.DLog(Helper.ToJsonStringPretty(it))
        }

        return ok(reply)
    }

    @Comment("测试方法2")
    @JsonApi(ReplyClass = StringReply::class)
    fun kktest2() : Result {
        val reply = StringReply()
        val path = "/ApiDocTemplates/ApiSample.html"
        val x = Helper::class.java.getResource(path)
        if (x != null) {
            Helper.DLog(x.toString())
            reply.result = x.toString()
            val ins = Helper::class.java.getResourceAsStream(path)
            IOUtils.readLines(ins, Charset.forName("UTF-8")).forEach { Helper.DLog(it) }
        }

        return ok(reply)
    }

}