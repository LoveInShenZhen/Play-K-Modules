package K.EventBus;

import K.Common.Helper;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import jodd.exception.ExceptionUtil;
import models.K.EventBus.EventException;

/**
 * Created by kk on 14/12/8.
 */
public class ExceptionLogHandler implements SubscriberExceptionHandler {

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {

        EventException exlog = new EventException();
        exlog.bus_name = context.getEventBus().getClass().getName();
        exlog.event_class = context.getEvent().getClass().getName();
        exlog.event_json = Helper.ToJsonStringPretty(context.getEvent());
        exlog.subscriber_class = context.getSubscriber().getClass().getName();
        exlog.subscriber_method = context.getSubscriberMethod().getName();
        exlog.exception = ExceptionUtil.exceptionChainToString(exception);

        exlog.save();
    }
}
