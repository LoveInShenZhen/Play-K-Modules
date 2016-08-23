package K.Controllers.ApiDoc.TemplateModel;

import K.Common.Helper;
import K.Controllers.ApiDoc.Reply.ApiInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 15/1/12.
 */
public class ApiGroup {
    public String groupName;
    public List<ApiInfo> apiInfoList;

    public ApiGroup(String group_name) {
        groupName = group_name;
        apiInfoList = new ArrayList<>();
    }

    public String ToMarkdownStr(String str) {
        return Helper.EscapeMarkdown(str);
    }


}
