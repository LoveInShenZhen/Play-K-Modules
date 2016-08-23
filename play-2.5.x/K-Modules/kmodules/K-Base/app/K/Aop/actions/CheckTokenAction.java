package K.Aop.actions;


import K.Aop.annotations.CheckToken;
import K.Common.Token;
import K.Common.TokenObject;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


/**
 * Created by kk on 13-12-16.
 */
public class CheckTokenAction extends Action<CheckToken> {

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        String token = ctx.request().getQueryString(this.configuration.token_arg());
        ReplyBase reply = new ReplyBase();
        if (StringUtils.isBlank(token)) {
            reply.ret = -3;
            reply.errmsg = "请登录";
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            return CompletableFuture.completedFuture(result);
        }

        if (Token.TokenTimeout(token)) {
            reply.ret = -3;
            reply.errmsg = "请重新登录";
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            return CompletableFuture.completedFuture(result);
        }

        TokenObject tokenObject = Token.GetTokenObject(token);

        if (this.configuration.allowed_roles().length > 0) {
            boolean has_role = false;
            for (int roleType : this.configuration.allowed_roles()) {
                if (tokenObject.HasRole(roleType)) {
                    has_role = true;
                    break;
                }
            }
            if (!has_role) {
                // 没有权限调用此 api
                reply.ret = -4;
                reply.errmsg = "没有权限调用此 api";
                Result result = JsonpController.ok(reply);
                return CompletableFuture.completedFuture(result);
            }
        }

        return delegate.call(ctx);
    }


    private String getApiUrl(Http.Context ctx) {
        String url = ctx.request().uri();
        int idx = url.indexOf("?");
        if (idx == -1) {
            return url;
        } else {
            return url.substring(0, idx);
        }
    }
}