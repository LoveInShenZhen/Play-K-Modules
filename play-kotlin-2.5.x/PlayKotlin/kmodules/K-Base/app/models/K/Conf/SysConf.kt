package models.K.Conf


import K.Aop.annotations.DBIndexed
import K.Aop.annotations.WithPersistLog
import com.avaje.ebean.Model
import play.api.i18n.Lang

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * Created by kk on 15/6/11.
 */

@WithPersistLog
@Entity
@Table(name = "k_sys_conf")
class SysConf : Model() {

    @Id
    var id: Long = 0

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(128) COMMENT '配置项名称'", nullable = false, unique = true)
    var conf_key: String? = null

    @Column(columnDefinition = "VARCHAR(256) COMMENT '配置项值'")
    var conf_value: String? = null

    @Column(columnDefinition = "VARCHAR(1024) COMMENT '配置项备注'")
    var ext_info: String? = null

    companion object {

        var find = Model.Finder<Long, SysConf>(SysConf::class.java)
    }

}
