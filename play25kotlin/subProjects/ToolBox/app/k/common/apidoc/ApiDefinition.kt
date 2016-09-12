package k.common.apidoc


import k.common.Helper

/**
 * Created by kk on 15/1/12.
 */
class ApiDefinition {

    var groups: MutableList<ApiGroup>

    init {
        groups = mutableListOf<ApiGroup>()

    }

    fun GetApiGroupByName(groupName: String): ApiGroup {

        var g = groups.find { it.groupName == groupName }

        if (g == null) {
            g = ApiGroup(groupName)
            groups.add(g)
        }

        return g
    }

    fun groupNames(): Set<String> {
        val names : MutableSet<String> = mutableSetOf()
        for (group in groups) {
            names.add(group.groupName)
        }
        return names
    }

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }
}
