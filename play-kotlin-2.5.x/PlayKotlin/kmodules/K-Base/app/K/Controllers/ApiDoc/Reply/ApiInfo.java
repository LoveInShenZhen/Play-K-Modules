package K.Controllers.ApiDoc.Reply;


import K.Aop.annotations.CheckToken;
import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.BizLogicException;
import K.Common.Helper;
import K.Controllers.ApiDoc.DocUtils;
import K.Controllers.JsonpController;
import K.DataDict.RoleType;
import K.Reply.ReplyBase;
import com.google.common.primitives.Primitives;
import jodd.util.ReflectUtil;
import play.Logger;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 14/11/5.
 */
public class ApiInfo {

    @Comment("API url")
    public String url;

    @Comment("API http method: GET or POST")
    public String http_method;

    @Comment("API 对应的 Controller 类名称")
    public String controller_class;

    @Comment("API 对应的 Controller 类下的方法名称")
    public String method_name;

    @Comment("POST 方法时, Form 表单 or JSON 对应的 类名称")
    public String post_data_class;

    @Comment("POST 方法时, Form 表单 or JSON 对应的 Sample")
    public String post_data_sample;

    @Comment("API 描述")
    public String method_comment;

    @Comment("返回Replay 对应的 java class name")
    public String reply_class;

    @Comment("返回Replay的描述信息")
    public ReplyInfo reply_info;

    @Comment("API 所有参数的描述")
    public List<FieldInfo> params;

    public static final String PostJson = "POST JSON";
    public static final String PostForm = "POST FORM";
    public static final String Get = "GET";

    public ApiInfo(String url,
                   String http_method,
                   String controller_class,
                   String method_name,
                   String reply_class)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        this.url = url;
        this.http_method = http_method;
        this.controller_class = controller_class;
        this.method_name = method_name;
        this.method_comment = "";
        this.reply_class = reply_class;
        params = new ArrayList<>();
        analyse();
    }

    public String ToMarkdownStr(String str) {
        return Helper.INSTANCE.EscapeMarkdown(str);
    }

    private void analyse() throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        analyseMethod();

        analyseReply();
    }

    private void analyseMethod() throws ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        // 分析 controller 方法信息
        Class controllerClass = Helper.INSTANCE.LoadClass(controller_class);
        Method method = ReflectUtil.findMethod(controllerClass, method_name);
        if (method == null) {
            throw new BizLogicException("Can not find method from Controller: %s by method_name='%s'",
                    controller_class,
                    method_name);
        }
        Comment method_comment_anno = method.getAnnotation(Comment.class);
        if (method_comment_anno != null) {
            this.method_comment = method_comment_anno.value();
        }

        if (!this.http_method.equalsIgnoreCase("GET")) {
            JsonApi api_anno = method.getAnnotation(JsonApi.class);
            if (api_anno.PostDataClass() != null) {
                this.post_data_class = api_anno.PostDataClass().getName();
                Object post_data_sample_obj = Helper.INSTANCE.LoadClass(this.post_data_class).newInstance();
                DocUtils.INSTANCE.SetupSampleDataForBasicTypeFields(post_data_sample_obj);
                this.post_data_sample = Helper.INSTANCE.ToJsonStringPretty(post_data_sample_obj);
            }
        }

        CheckToken check_token_anno = method.getAnnotation(CheckToken.class);
        if (check_token_anno != null && check_token_anno.allowed_roles().length > 0) {
            StringBuilder role_comment = new StringBuilder();
            role_comment.append(", 要求具备角色:");
            for (int require_role : check_token_anno.allowed_roles()) {
                RoleType role_type = RoleType.GetRoleType(require_role);
                if (role_type == null) {
                    Logger.error("API: {}.{} 设置了错误的角色值: {}",
                            controllerClass.getName(),
                            method.getName(),
                            require_role);
                    continue;
                }
                role_comment.append(String.format(" %d-%s;", require_role, role_type.desc));
            }

            this.method_comment = this.method_comment + role_comment.toString();
        }

        // 分析方法的参数信息
        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            if (!param.isNamePresent()) {
                throw new BizLogicException("Parameter names are not present! Please passed -parameters argument to your Java 8 compiler");
            }
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.field_name = param.getName();
            fieldInfo.field_type = Primitives.wrap(param.getType()).getSimpleName();

            Comment param_comment_anno = param.getAnnotation(Comment.class);
            if (param_comment_anno != null) {
                fieldInfo.comments = param_comment_anno.value();
            }

            params.add(fieldInfo);
        }
    }

    private void analyseReply() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        // 分析返回的 reply 的信息
        Class reply_clazz = Helper.INSTANCE.LoadClass(this.reply_class);
        ReplyBase reply = (ReplyBase) reply_clazz.newInstance();
        reply_info = new ReplyInfo();
        reply_info.reply_class_name = this.reply_class;
        reply.SetupSampleData();
        reply_info.sample = Helper.INSTANCE.ToJsonStringPretty(reply);

        DocUtils.INSTANCE.FindFieldWithComments(Helper.INSTANCE.LoadClass(this.reply_class),
                reply_info.fields_with_comments);
    }

    public String TestPage() {
        return String.format("http://%s/api/doc/GeneratApiSample?api_url=%s",
                JsonpController.request().host(),
                this.url);
    }

    public String LocalTestPage() {
        return String.format("http://%s/api/doc/GeneratApiSample?api_url=%s",
                JsonpController.request().host(),
                this.url);
    }

    public boolean IsGetJsonApi() {
        return this.http_method.equalsIgnoreCase("GET");
    }

    public boolean IsPostJsonApi() {
        return this.http_method.equalsIgnoreCase("POST JSON");
    }

    public boolean IsPostFormApi() {
        return this.http_method.equalsIgnoreCase("POST FORM");
    }

    public List<FieldInfo> PostFormFieldInfos() {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        if (IsPostFormApi()) {
            Class form_class = Helper.INSTANCE.LoadClass(this.post_data_class);
            Field[] form_fields = ReflectUtil.getAccessibleFields(form_class);
            for (Field field : form_fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    // 过滤掉 static field
                    continue;
                }
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.field_name = field.getName();
                fieldInfo.field_type = field.getType().getSimpleName();

                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) {
                    fieldInfo.comments = comment.value();
                } else {
                    fieldInfo.comments = "";
                }

                fieldInfoList.add(fieldInfo);
            }
//            if (this.post_data_class.equals(UploadForm.class.getName())) {
//                FieldInfo fieldInfo = new FieldInfo();
//                fieldInfo.field_name = "attachmentFile";
//                fieldInfo.field_type = "FilePart";
//                fieldInfo.comments = "上次文件";
//                fieldInfo.input_type = "file";
//
//                fieldInfoList.add(fieldInfo);
//            }
        }
        return fieldInfoList;
    }
}
