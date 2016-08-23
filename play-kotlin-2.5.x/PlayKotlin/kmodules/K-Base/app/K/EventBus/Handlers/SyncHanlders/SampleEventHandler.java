package K.EventBus.Handlers.SyncHanlders;

import K.Common.BizLogicException;
import K.EventBus.Events.SampleEvent;
import K.Common.Helper;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import play.Logger;


/**
 * Created by kk on 15/6/9.
 */
public class SampleEventHandler {

    @Subscribe
    @AllowConcurrentEvents
    public void OnSampleEvent(SampleEvent event) {
//        Logger.debug("{} TestEventListener Thread id: {}", Helper.Date2Str(new Date(), "HH:mm:ss.SSSS"), Thread.currentThread().getId());
        Logger.debug("==> Received Event: {}", Helper.INSTANCE.ToJsonStringPretty(event));
        throw new BizLogicException("模拟产生异常, thread id: %d", Thread.currentThread().getId());
//        Logger.debug("{} Finish handle event: {}", Helper.Date2Str(new Date(), "HH:mm:ss.SSSS"), Thread.currentThread().getId());
    }

}
