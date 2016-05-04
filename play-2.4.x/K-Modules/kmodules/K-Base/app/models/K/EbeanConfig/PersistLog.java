package models.K.EbeanConfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

/**
 * Created by kk on 14/11/28.
 */
public class PersistLog {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    public Date action_time;
    public String action_type;
    public String bean_class;
    public JsonNode bean_data;

    public PersistLog() {
        action_time = new Date();
    }
}
