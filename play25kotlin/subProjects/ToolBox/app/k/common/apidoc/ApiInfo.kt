package k.common.apidoc

import k.aop.annotations.Comment
import k.common.Helper
import play.mvc.Controller
import kotlin.reflect.KClass

/**
 * Created by kk on 14/11/5.
 */
class ApiInfo
constructor(
        @Comment("API url")
        val url: String,

        @Comment("API http method: GET or POST")
        val httpMethod: String,

        @Comment("API 对应的 Controller 类名称")
        val controllerClass: String,

        @Comment("API 对应的 Controller 类下的方法名称")
        val methodName: String,

        private val replyKClass: KClass<*>,

        private val postDataKClass: KClass<*>) {

    @Comment("返回Replay 对应的 java class name")
    var replyClass: String = ""

    @Comment("POST 方法时, Form 表单 or JSON 对应的 类名称")
    var postDataClass: String = ""

    @Comment("POST 方法时, Form 表单 or JSON 对应的 Sample")
    var postDataSample: String = ""

    @Comment("API 描述")
    var apiComment: String = ""

    @Comment("返回Replay的描述信息")
    var replyInfo: FieldSchema? = null

    @Comment("API 所有参数的描述")
    var params: MutableList<FieldSchema> = mutableListOf<FieldSchema>()

    init {
        replyClass = replyKClass.javaObjectType.name
        postDataClass = postDataKClass?.javaObjectType.name ?: ""
        analyse()
    }

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }

    private fun analyse() {

        analyseMethod()

        analyseReply()
    }

    private fun analyseMethod() {
        // 分析 controller 方法信息
    }

    private fun analyseReply() {
        // 分析返回的 reply 的信息
//        val reply_clazz = Helper.LoadClass(this.reply_class)
//        val reply = reply_clazz!!.newInstance() as ReplyBase
//        replyInfo = ReplyInfo()
//        replyInfo.reply_class_name = this.reply_class
//        reply.SetupSampleData()
//        replyInfo.sample = Helper.ToJsonStringPretty(reply)
//
//        DocUtils.FindFieldWithComments(Helper.LoadClass(this.reply_class)!!,
//                replyInfo.fields_with_comments)
    }

    fun TestPage(): String {
        return String.format("http://%s/api/doc/GeneratApiSample?api_url=%s",
                Controller.request().host(),
                this.url)
    }

    fun LocalTestPage(): String {
        return String.format("http://%s/api/doc/GeneratApiSample?api_url=%s",
                Controller.request().host(),
                this.url)
    }

    fun IsGetJsonApi(): Boolean {
        return this.httpMethod.equals("GET", ignoreCase = true)
    }

    fun IsPostJsonApi(): Boolean {
        return this.httpMethod.equals("POST JSON", ignoreCase = true)
    }

    fun IsPostFormApi(): Boolean {
        return this.httpMethod.equals("POST FORM", ignoreCase = true)
    }

    fun PostFormFieldInfos(): MutableList<FieldSchema> {
//        val fieldInfoList = kotlin.collections.MutableList<FieldInfo>()
//        if (IsPostFormApi()) {
//            val form_class = Helper.LoadClass(this.postDataClass)
//            val form_fields = ReflectUtil.getAccessibleFields(form_class!!)
//            for (field in form_fields) {
//                if (Modifier.isStatic(field.modifiers)) {
//                    // 过滤掉 static field
//                    continue
//                }
//                val fieldInfo = FieldInfo()
//                fieldInfo.field_name = field.name
//                fieldInfo.field_type = field.type.simpleName
//
//                val comment = field.getAnnotation(Comment::class.java)
//                if (comment != null) {
//                    fieldInfo.comments = comment.value
//                } else {
//                    fieldInfo.comments = ""
//                }
//
//                fieldInfoList.add(fieldInfo)
//            }
//            //            if (this.postDataClass.equals(UploadForm.class.getName())) {
//            //                FieldInfo fieldInfo = new FieldInfo();
//            //                fieldInfo.field_name = "attachmentFile";
//            //                fieldInfo.field_type = "FilePart";
//            //                fieldInfo.comments = "上次文件";
//            //                fieldInfo.input_type = "file";
//            //
//            //                fieldInfoList.add(fieldInfo);
//            //            }
//        }
//        return fieldInfoList

        val fieldInfoList = mutableListOf<FieldSchema>()

        return fieldInfoList
    }

    companion object {

        val PostJson = "POST JSON"
        val PostForm = "POST FORM"
        val Get = "GET"
    }
}
