package K.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.libs.Jsonp;
import play.mvc.Controller;
import play.mvc.Http;

/**
 * Created by kk on 14-4-25.
 */
public class JsonpController extends Controller {

    public static Status ok(JsonNode content) {
        String callback = Http.Context.current().request().getQueryString("callback");
        if (StringUtils.isBlank(callback)) {
            return Controller.ok(content);
        } else {
            return ok(new Jsonp(callback, content));
        }
    }

    public static String ClientIp() {
        String real_ip = request().getHeader("X-Real-IP");
        if (real_ip == null) {
            return request().remoteAddress();
        }
        return real_ip;
    }
}
