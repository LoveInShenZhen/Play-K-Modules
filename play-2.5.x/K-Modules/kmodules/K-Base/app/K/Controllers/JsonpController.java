package K.Controllers;

import K.Reply.ReplyBase;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.util.StringUtil;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import play.libs.Jsonp;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Created by kk on 14-4-25.
 */
public class JsonpController extends Controller {


    public static Result ok(JsonNode content) {

        JsonpController.ctx().args.put("api_reply", content);

        String callback = Http.Context.current().request().getQueryString("callback");
        if (StringUtils.isBlank(callback)) {
            return Controller.ok(content);
        } else {
            return ok(new Jsonp(callback, content));
        }
    }

    public static Result ok(ReplyBase reply) {
        return JsonpController.ok(reply.ToJsonNode());
    }

//    Nginx 配置:
//
//    proxy_http_version 1.1;
//    proxy_set_header   Host             $http_host;
//    proxy_set_header   X-Real-IP        $remote_addr;
//    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;

    public static String ClientIp() {
        String real_ip = request().getHeader("X-Real-IP");
        if (StringUtil.isBlank(real_ip)) {
            return request().remoteAddress();
        }

        return real_ip;
    }

    public static boolean ProxyHttps() {
        String use_ssl = request().getHeader("X-Use-SSL");

        if (StringUtil.isBlank(use_ssl)) {
            return request().secure();
        }

        if (use_ssl.equalsIgnoreCase("true") || use_ssl.equalsIgnoreCase("yes")) {
            return true;
        } else {
            return false;
        }
    }

    public static String Protocol() {
        if (ProxyHttps()) {
            return "https";
        } else {
            return "http";
        }
    }

    public static Result Download(String fileName, byte[] content) throws EncoderException {
        Http.Response response = response();
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", ContentDisposition(fileName));

        return ok(content);
    }

    private static String ContentDisposition(String file_name) throws EncoderException {
        URLCodec urlCodec = new URLCodec("UTF-8");
        String file_name_url_ecode = urlCodec.encode(file_name);
        String content_disposition;
        if (request().getHeader(Controller.USER_AGENT).indexOf("IE") > 0) {
            content_disposition = String.format("attachment; filename=%s", file_name_url_ecode);
        } else {
            content_disposition = String.format("attachment; filename*=UTF-8''%s", file_name_url_ecode);
        }
        return content_disposition;
    }
}
