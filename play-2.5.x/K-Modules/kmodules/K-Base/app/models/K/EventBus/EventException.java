package models.K.EventBus;

import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by kk on 15/8/25.
 */

@Entity
@Table(name = "k_event_exception")
public class EventException extends Model {

    @Id
    public long id;

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Bus Name'")
    public String bus_name;

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Object Class Name'")
    public String event_class;

    @Column(columnDefinition = "TEXT COMMENT 'Event Object Json Data'")
    public String event_json;

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Subscriber Class Name'")
    public String subscriber_class;

    @Column(columnDefinition = "VARCHAR(256) COMMENT 'Event Subscriber Method Name'")
    public String subscriber_method;

    @Column(columnDefinition = "TEXT COMMENT 'Exception Message'")
    public String exception;

    public static Finder<Long, EventException> find = new Finder<Long, EventException>(EventException.class);

}
