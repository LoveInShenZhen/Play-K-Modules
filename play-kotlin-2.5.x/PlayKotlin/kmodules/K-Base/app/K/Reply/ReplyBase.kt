package K.Reply

import K.Common.Helper
import com.fasterxml.jackson.databind.JsonNode
import K.Controllers.ApiDoc.DocUtils
import jodd.exception.ExceptionUtil
import play.Logger
import play.libs.Json

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-15
 * Time: 下午5:03
 * To change this template use File | Settings | File Templates.
 */

open class ReplyBase {

    var ret: Int = 0
    var errmsg: String
    var errors: JsonNode? = null

    init {
        ret = 0
        errmsg = "OK"
        errors = null
    }

    fun SetupSampleData() {
        try {
            ret = 0
            errmsg = "OK"
            DocUtils.SetupSampleDataForBasicTypeFields(this)
        } catch (ex: Exception) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex))
        }

    }

    fun ToJsonStr(): String {
        return Helper.ToJsonStringPretty(this) ?: ""
    }

    override fun toString(): String {
        return ToJsonStr()
    }

    fun ToJsonNode(): JsonNode {
        return Json.toJson(this)
    }
}
