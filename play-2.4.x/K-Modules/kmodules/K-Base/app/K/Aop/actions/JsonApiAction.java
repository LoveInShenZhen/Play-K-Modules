package K.Aop.actions;



import K.BizLogicException;
import K.Helper;
import K.DB;
import K.Reply.ReplyBase;
import K.Aop.annotations.JsonApi;
import com.avaje.ebean.TxCallable;
import com.avaje.ebean.TxIsolation;
import com.avaje.ebean.TxScope;
import K.Controllers.JsonpController;
import jodd.exception.ExceptionUtil;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by kk on 14-6-10.
 */
public class JsonApiAction extends Action<JsonApi> {

    private Date before_call;

    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        before_call = new Date();
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
        boolean record_api = play.Configuration.root().getBoolean("jsonapi.log", false);
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

    private F.Promise<Result> call_api(final Http.Context ctx) {
        try {
            try {
                F.Promise<Result> resultPromise = delegate.call(ctx);
                log("");
                return resultPromise;
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }

        } catch (BizLogicException ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = ex.ErrCode;
            reply.errmsg = ex.getMessage();
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return F.Promise.pure(result);

        } catch (Exception ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = Helper.StackTraceOfEx(ex);
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return F.Promise.pure(result);
        }
    }

    private F.Promise<Result> call_api_with_tran(final Http.Context ctx) {
        try {
            TxScope txScope = TxScope.requiresNew().setIsolation(TxIsolation.READ_COMMITED);
            TxCallable<F.Promise<Result>> txCallable = new TxCallable<F.Promise<Result>>() {
                @Override
                public F.Promise<Result> call() {
                    try {
                        return delegate.call(ctx);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            };
            F.Promise<Result> resultPromise = DB.ReadWriteDB().execute(txScope, txCallable);
            return resultPromise;

        } catch (BizLogicException ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = ex.ErrCode;
            reply.errmsg = ex.getMessage();
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return F.Promise.pure(result);

        } catch (Exception ex) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = ExceptionUtil.exceptionToString(ex);
            Logger.warn(ExceptionUtil.exceptionChainToString(ex));
            Result result = play.mvc.Results.ok(Json.toJson(reply));
            handleApiException(ctx, ex);
            log(ex.getMessage());
            return F.Promise.pure(result);
        }
    }

    private void handleApiException(final Http.Context ctx, Exception ex) {
        // todo send email when has json exceptions

    }

}
