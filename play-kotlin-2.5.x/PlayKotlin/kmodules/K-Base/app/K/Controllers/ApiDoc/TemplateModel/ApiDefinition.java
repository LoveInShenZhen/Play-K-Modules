package K.Controllers.ApiDoc.TemplateModel;

import K.Common.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by kk on 15/1/12.
 */
public class ApiDefinition {
    public List<ApiGroup> groups;

    public ApiDefinition() {
        groups = new ArrayList<>();
    }

    public ApiGroup GetApiGroupByName(String group_name) {
        for (ApiGroup group : groups) {
            if (group.groupName.equals(group_name)) {
                return group;
            }
        }
        // 找不到 group, 则根据group_name, 创建一个新的
        ApiGroup group = new ApiGroup(group_name);
        groups.add(group);
        return group;
    }

    public Set<String> groupNames() {
        Set<String> names = new TreeSet<>();
        for (ApiGroup group : groups) {
            names.add(group.groupName);
        }
        return names;
    }

    public String ToMarkdownStr(String str) {
        return Helper.INSTANCE.EscapeMarkdown(str);
    }
}
