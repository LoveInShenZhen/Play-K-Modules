package models.K.EventBus

import com.avaje.ebean.Model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by kk on 15/8/25.
 */

@Entity
@Table(name = "k_event_exception")
class EventException : Model() {

    @Id
    var id: Long = 0

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Bus Name'")
    var bus_name: String? = null

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Object Class Name'")
    var event_class: String? = null

    @Column(columnDefinition = "TEXT COMMENT 'Event Object Json Data'")
    var event_json: String? = null

    @Column(columnDefinition = "VARCHAR(1024) COMMENT 'Event Subscriber Class Name'")
    var subscriber_class: String? = null

    @Column(columnDefinition = "VARCHAR(256) COMMENT 'Event Subscriber Method Name'")
    var subscriber_method: String? = null

    @Column(columnDefinition = "TEXT COMMENT 'Exception Message'")
    var exception: String? = null

    companion object {

        var find = Model.Finder<Long, EventException>(EventException::class.java)
    }

}
