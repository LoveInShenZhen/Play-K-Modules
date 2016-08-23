package K.Reply;

import K.Common.Helper;
import K.Controllers.ApiDoc.DocUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.exception.ExceptionUtil;
import play.Logger;
import play.libs.Json;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-15
 * Time: 下午5:03
 * To change this template use File | Settings | File Templates.
 */

public class ReplyBase {

    public int ret;
    public String errmsg;
    public JsonNode errors;

    public ReplyBase() {
        ret = 0;
        errmsg = "OK";
        errors = null;
    }

    public void SetupSampleData() {
        try {
            ret = 0;
            errmsg = "OK";
            DocUtils.SetupSampleDataForBasicTypeFields(this);
        } catch (Exception ex) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex));
        }

    }

    public String ToJsonStr() {
        return Helper.ToJsonStringPretty(this);
    }

    @Override
    public String toString() {
        return ToJsonStr();
    }

    public JsonNode ToJsonNode() {
        return Json.toJson(this);
    }
}
