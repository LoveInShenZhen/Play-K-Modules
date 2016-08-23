package K.Controllers.ApiDoc.Reply;


import K.Aop.annotations.Comment;
import K.Common.Helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 14/11/5.
 */
public class ReplyInfo {
    @Comment("Reply 的事例数据")
    public String sample;

    @Comment("Reply 包含的字段列表")
    public List<FieldInfo> fields_with_comments;

    @Comment("Reply 对应的 java class")
    public String reply_class_name;

    public ReplyInfo() {
        fields_with_comments = new ArrayList<>();
    }

    public String ToMarkdownStr(String str) {
        return Helper.EscapeMarkdown(str);
    }
}
