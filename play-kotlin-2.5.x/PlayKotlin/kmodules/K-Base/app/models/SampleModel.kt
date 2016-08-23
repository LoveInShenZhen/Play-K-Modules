package models

import com.avaje.ebean.Model
import jodd.datetime.JDateTime

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Created by kk on 15/7/11.
 */

@Entity
class SampleModel : Model() {

    @Id
    var id: Long = 0

    @Column(columnDefinition = "DATETIME COMMENT '任务计划执行时间'")
    var plan_run_time: JDateTime? = null

    companion object {

        var find = Model.Finder<Long, SampleModel>(SampleModel::class.java)
    }
}
