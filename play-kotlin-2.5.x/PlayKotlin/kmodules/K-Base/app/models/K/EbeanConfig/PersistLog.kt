package models.K.EbeanConfig

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.JsonNode

import java.util.Date

/**
 * Created by kk on 14/11/28.
 */
class PersistLog {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    var action_time: Date
    var action_type: String? = null
    var bean_class: String? = null
    var bean_data: JsonNode? = null

    init {
        action_time = Date()
    }
}
