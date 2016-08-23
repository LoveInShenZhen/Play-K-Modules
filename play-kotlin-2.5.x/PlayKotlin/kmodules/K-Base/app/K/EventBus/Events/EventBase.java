package K.EventBus.Events;

import K.Common.Helper;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Created by kk on 14/11/8.
 */
public abstract class EventBase {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSS", timezone = "Asia/Shanghai")
    public Date create_time;

    public String ToJson() {
        return Helper.INSTANCE.ToJsonStringPretty(this);
    }

    public EventBase() {
        create_time = new Date();
    }
}
