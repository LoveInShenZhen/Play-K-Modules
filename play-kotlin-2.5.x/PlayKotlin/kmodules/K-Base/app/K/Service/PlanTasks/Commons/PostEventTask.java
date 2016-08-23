package K.Service.PlanTasks.Commons;


import K.Common.Helper;
import K.EventBus.EventBusService;
import K.EventBus.Events.EventBase;
import models.K.BgTask.PlanTask;

import java.util.Date;

/**
 * Created by kk on 15/10/13.
 */
public class PostEventTask implements Runnable {

    public String event_class;

    public String event_json;

    @Override
    public void run() {
        EventBase event_obj = (EventBase) Helper.INSTANCE.FromJsonString(this.event_json, Helper.INSTANCE.LoadClass(this.event_class));
        EventBusService.PostAsyncEvent(event_obj);
    }

    public static void PostEvent(PostEventTask event_task) {
        PlanTask.addTask(event_task, "EventBus", false, new Date());
    }

    public static void PostEvent(EventBase event) {
        PostEventTask task = new PostEventTask();
        task.event_class = event.getClass().getName();
        task.event_json = event.ToJson();
        PostEvent(task);
    }
}
