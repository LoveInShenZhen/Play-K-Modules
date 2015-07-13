package K.EventBus;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import jodd.exception.ExceptionUtil;
import play.Configuration;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 14/12/8.
 */
public class ExceptionHandler implements SubscriberExceptionHandler {

    private ArrayList<SubscriberExceptionHandler> handlers;

    public ExceptionHandler() {
        handlers = new ArrayList<>();
        loadHandlersByConfig();
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        for (SubscriberExceptionHandler handler : handlers) {
            handler.handleException(exception, context);
        }
    }

    private void loadHandlersByConfig() {
        try {
            List<String> defautClsNames = new ArrayList<>();
            List<String> clsNameList = Configuration.root().getStringList("EventBus.ExceptionHandlers", defautClsNames);
            if (!clsNameList.contains(ExceptionLogHandler.class.getName())) {
                clsNameList.add(ExceptionLogHandler.class.getName());
            }
            for (String clsName : clsNameList) {
                SubscriberExceptionHandler handler = (SubscriberExceptionHandler) ExceptionHandler.class
                        .getClassLoader()
                        .loadClass(clsName)
                        .newInstance();
                handlers.add(handler);
            }
        } catch (Exception ex) {
            Logger.error(ExceptionUtil.exceptionChainToString(ex));
        }

    }

}
