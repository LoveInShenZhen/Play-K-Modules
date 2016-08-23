package K.Controllers.ApiDoc.Reply;


import K.Common.Helper;
import K.Aop.annotations.Comment;

/**
 * Created by kk on 14/11/5.
 */
public class FieldInfo {
    @Comment("方法参数或者返回结果 Reply 中的字段名称")
    public String field_name;

    @Comment("字段描述")
    public String comments;

    @Comment("字段的数据类型")
    public String field_type;

    @Comment("字段所属的对象的 Class 名称")
    public String ownner_class_name;

    @Comment("表单 input 类型, 默认text")
    public String input_type = "text";

    public String ToMarkdownStr(String str) {
        return Helper.INSTANCE.EscapeMarkdown(str);
    }
}
