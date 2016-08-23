package K.EventBus;

import K.Common.Helper;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import jodd.exception.ExceptionUtil;
import models.K.EventBus.EventException;
import play.Logger;

/**
 * Created by kk on 14/12/8.
 */
public class ExceptionLogHandler implements SubscriberExceptionHandler {

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {

        EventException exlog = new EventException();
        exlog.setBus_name(context.getEventBus().getClass().getName());
        exlog.setEvent_class(context.getEvent().getClass().getName());
        exlog.setEvent_json(Helper.INSTANCE.ToJsonStringPretty(context.getEvent()));
        exlog.setSubscriber_class(context.getSubscriber().getClass().getName());
        exlog.setSubscriber_method(context.getSubscriberMethod().getName());
        exlog.setException(ExceptionUtil.exceptionChainToString(exception));

        exlog.save();
    }
}
