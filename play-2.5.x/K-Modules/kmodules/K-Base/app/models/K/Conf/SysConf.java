package models.K.Conf;


import K.Aop.annotations.DBIndexed;
import K.Aop.annotations.WithPersistLog;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by kk on 15/6/11.
 */

@WithPersistLog
@Entity
@Table(name = "k_sys_conf")
public class SysConf extends Model {

    @Id
    public long id;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(128) COMMENT '配置项名称'", nullable = false, unique = true)
    public String conf_key;

    @Column(columnDefinition = "TEXT COMMENT '配置项值'")
    public String conf_value;

    @Column(columnDefinition = "TEXT COMMENT '配置项备注'")
    public String ext_info;

    public static Finder<Long, SysConf> find = new Finder<Long, SysConf>(SysConf.class);

}
