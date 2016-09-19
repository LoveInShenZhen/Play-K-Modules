package k.common.apidoc

import k.aop.annotations.Comment
import k.aop.annotations.JsonApi
import k.common.Helper
import k.common.json.JsonDataType
import play.mvc.Controller
import kotlin.reflect.KClass
import kotlin.reflect.functions
import kotlin.reflect.jvm.javaType

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

        replyKClass: KClass<*>,

        postDataKClass: KClass<*>) {

    @Comment("返回Replay 对应的 java class name")
    var replyClass: String = ""

    @Comment("POST 方法时, Form 表单 or JSON 对应的 类名称")
    var postDataClass: String = ""

//    @Comment("POST 方法时, Form 表单 or JSON 对应的 Sample")
//    var postDataSample: String = ""

    @Comment("API 描述")
    var apiComment: String = ""

    @Comment("返回Replay的描述信息")
    var replyInfo: FieldSchema

    @Comment("API 所有参数的描述")
    var params: List<ParameterInfo> = emptyList()

    init {
        replyClass = replyKClass.javaObjectType.name
        postDataClass = postDataKClass.javaObjectType.name


        if (httpMethod == ApiInfo.Get) {
            postDataClass = ""
        }

        replyInfo = FieldSchema()
        replyInfo.level = 0
        replyInfo.name = "reply"
        replyInfo.desc = ""
        replyInfo.type = JsonDataType.OBJECT.typeName

        analyse()
    }

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }

    fun groupName(): String {
        val controllerClazz = Class.forName(this.controllerClass)
        val anno = controllerClazz.getAnnotation(Comment::class.java)
        return anno?.value ?: this.controllerClass
    }

    private fun analyse() {

        analyseMethod()

        analyseReply()
    }

    private fun analyseMethod() {
        // 分析 controller 方法信息
        val controllerKClazz = Class.forName(this.controllerClass).kotlin
        val method = controllerKClazz.functions.find { it.name == this.methodName }
        val commentAnno = method!!.annotations.find { it is Comment }
        if (commentAnno != null && commentAnno is Comment) {
            this.apiComment = commentAnno.value
        }

        this.params = method.parameters
                .filter { it.name != null }
                .map {
                    var paramDesc = ""
                    val paramComment = it.annotations.find { it is Comment }
                    if (paramComment != null && paramComment is Comment) {
                        paramDesc = paramComment.value
                    }
                    ParameterInfo(name = it.name!!,
                            desc = paramDesc,
                            type = it.type.javaType.typeName.split(".").last())
                }

        val jsonApiAnno = method.annotations.find { it is JsonApi } as JsonApi

        if (postDataClass.isNotBlank()) {
            if (jsonApiAnno.ApiMethodType == ApiInfo.PostForm) {
                // todo 构造 form 表单
            }

            if (jsonApiAnno.ApiMethodType == ApiInfo.PostJson) {
                // todo 构造 post 的 JSON 字符串
            }
        }

    }

    private fun analyseReply() {
        // 分析返回的 reply 的信息
        FieldSchema.resolveFields(Class.forName(this.replyClass).kotlin, replyInfo)
    }

    fun TestPage(): String {
        return "http://${Controller.request().host()}/apiSample?apiUrl=${this.url}"
    }

    fun IsGetJsonApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.Get, ignoreCase = true)
    }

    fun IsPostJsonApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.PostJson, ignoreCase = true)
    }

    fun IsPostFormApi(): Boolean {
        return this.httpMethod.equals(ApiInfo.PostForm, ignoreCase = true)
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
