package K.EventBus;

import K.Common.Helper;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import jodd.exception.ExceptionUtil;
import play.Logger;

/**
 * Created by kk on 14/12/8.
 */
public class ExceptionLogHandler implements SubscriberExceptionHandler {

    private String log_name = "EventBus";

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(String.format("EventBus: %s\n", context.getEventBus().getClass().getName()));
        sb.append(String.format("Event Class: %s\n", context.getEvent().getClass().getName()));
        sb.append(String.format("Event: %s\n", Helper.ToJsonStringPretty(context.getEvent())));
        sb.append(String.format("Subscriber: %s\n", context.getSubscriber().getClass().getName()));
        sb.append(String.format("SubscriberMethod: %s\n", context.getSubscriberMethod().getName()));
        sb.append(String.format("Exception: %s", ExceptionUtil.exceptionToString(exception)));
        sb.append("========================================\n\n");

        Logger.of(log_name).error(sb.toString());
        Logger.debug(sb.toString());

        throw new RuntimeException(exception);
    }
}
