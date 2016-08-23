package K.Aop.actions;


import K.Aop.annotations.JsonApi;
import K.Common.BizLogicException;
import K.Common.Helper;
import K.Common.Hub;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxCallable;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxScope;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.exception.ExceptionUtil;
import play.Logger;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by kk on 14-6-10.
 */
public class JsonApiAction extends Action<JsonApi> {

    private Date before_call;

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        before_call = new Date();

        if (this.configuration.UseEtag() && this.configuration.Transactional()) {
            // Etag 只能用于查询, 不能用于交易类(只对数据库有更新操作)的 api 上
            // 所以, UseEtag 为 True 的时候, Transactional 必须是设置成 False, 否则抛出异常
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = "UseEtag 为 True 的时候, Transactional 必须是设置成 False";
            Result result = JsonpController.ok(reply);
            return CompletableFuture.completedFuture(result);
        }

        if (this.configuration.Transactional()) {
            return call_api_with_tran(ctx);
        } else {
            return call_api(ctx);
        }
    }

    private String getHostName() {
        try {
            InetAddress IP = InetAddress.getLocalHost();
            return IP.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown Host Name";
        }
    }

    private void log(String ex_msg) {
        boolean record_api = Hub.configuration().getBoolean("jsonapi.log", false);
        if (record_api) {
            Logger.ALogger logger = Logger.of("jsonapi");
            UUID log_guid = UUID.randomUUID();
            Http.Request request = JsonpController.request();
            Date now = new Date();

            String del_sql = String.format("delete from api_log where guid='%s';", log_guid.toString());
            String ins_sql = String.format("insert api_log (`guid`, `log_time`, `api_method`, `api_path`, `api_url`, "
                            + "`form_data`, `spend_time`, `host_name`, `client_ip`, `exceptions`) "
                            + "values ('%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s');",
                    log_guid.toString(),
                    Helper.Date2Str(new Date(), Helper.DB_DATE_FMT),
                    request.method(),
                    request.path(),
                    request.uri(),
                    Helper.ToJsonStringPretty(request.body().asJson()),
                    Long.toString(now.getTime() - this.before_call.getTime()),
                    this.getHostName(),
                    JsonpController.ClientIp(),
                    ex_msg
            );
            logger.info(del_sql);
            logger.info(ins_sql);
        }
    }

    private CompletionStage<Result> call_api(final Http.Context ctx) {
        try {
            try {
                CompletionStage<Result> api_result = delegate.call(ctx);

                if (this.configuration.UseEtag() && ctx.args.containsKey("api_reply")) {
                    // Api 使用 Etag 方式, 来标记Api的查询结果是否和上次一致(木有发生变化)
                    JsonNode api_reply = (JsonNode) ctx.args.get("api_reply");
                    String the_etag = Helper.SHA1OfString(api_reply.toString());

                    // 保存本次查询结果的 Etag
                    ctx.response().setHeader(Controller.ETAG, the_etag);

                    if (ctx.request().hasHeader(Controller.IF_NONE_MATCH)) {
                        // 需要检查, 本次查询结果和上一次的是否一致, 如果一致, 返回 304
                        String last_etag = ctx.request().getHeader(Controller.IF_NONE_MATCH);
                        if (the_etag.equals(last_etag)) {
                            // 和上次的查询结果一致
                            return CompletableFuture.completedFuture(status(Controller.NOT_MODIFIED));
                        } else {
                            return api_result;
                        }
                    } else {
                        // 没有 If-None-Match header
                        return api_result;
                    }
                } else {
                    // 不使用 Etag
                    return api_result;
                }

            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

        } catch (BizLogicException ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = ex.ErrCode;
            reply.errmsg = ex.getMessage();
            Result result = JsonpController.ok(reply);
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return CompletableFuture.completedFuture(result);

        } catch (Exception ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = Helper.StackTraceOfEx(ex);
            Result result = JsonpController.ok(reply);
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return CompletableFuture.completedFuture(result);
        }
    }

    private CompletionStage<Result> call_api_with_tran(final Http.Context ctx) {
        try {
            TxScope txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED);
            TxCallable<CompletionStage<Result>> txCallable = () -> {
                try {
                    return delegate.call(ctx);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };

            return Ebean.execute(txScope, txCallable);

        } catch (BizLogicException ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = ex.ErrCode;
            reply.errmsg = ex.getMessage();
            Result result = JsonpController.ok(reply);
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return CompletableFuture.completedFuture(result);

        } catch (Exception ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = ExceptionUtil.exceptionChainToString(ex);
            Logger.warn(ExceptionUtil.exceptionChainToString(ex));
            Result result = JsonpController.ok(reply);
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return CompletableFuture.completedFuture(result);
        }
    }

    private void handleApiException(final Http.Context ctx, Exception ex) {
        // todo send email when has json exceptions

    }

}
