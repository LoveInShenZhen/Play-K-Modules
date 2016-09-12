package k.common.apidoc


import k.common.Helper

/**
 * Created by kk on 15/1/12.
 */
class ApiGroup(val groupName: String) {

    var apiInfoList: MutableList<ApiInfo>

    init {
        apiInfoList = mutableListOf<ApiInfo>()
    }

    fun ToMarkdownStr(str: String): String {
        return Helper.EscapeMarkdown(str)
    }


}
