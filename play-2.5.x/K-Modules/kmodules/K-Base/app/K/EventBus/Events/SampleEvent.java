package K.EventBus.Events;

/**
 * Created by kk on 14/11/9.
 */
public class SampleEvent extends EventBase {
    public String EventMsg;

    public SampleEvent(String msg) {
        this.EventMsg = msg;
    }

}
