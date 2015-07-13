package K.EventBus.Events;

import K.Helper;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Created by kk on 14/11/8.
 */
public abstract class EventBase {

    private Date create_time;

    public String ToJson() {
        return Helper.ToJsonStringPretty(this);
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSS", timezone = "Asia/Shanghai")
    public Date getCreateTime() {
        return create_time;
    }

    public EventBase() {
        create_time = new Date();
    }
}
