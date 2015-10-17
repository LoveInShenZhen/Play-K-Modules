package models.K;

import K.Aop.annotations.DBIndexed;
import K.Aop.annotations.WithPersistLog;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Created by kk on 15/8/27.
 */

@WithPersistLog
@Entity
@Table(name = "k_users")
public class Users extends Model {

    @Id
    public long id;

    @DBIndexed
    @Column(columnDefinition = "CHAR(40) COMMENT '用户 UUID'", unique = true, nullable = false)
    public UUID user_id;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(64) DEFAULT NULL COMMENT '用户名'", unique = true)
    public String user_name;

    @Column(columnDefinition = "VARCHAR(128) COMMENT '二次防字典工具加密后的密码'")
    public String password;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(16) DEFAULT NULL COMMENT '用户手机'", unique = true)
    public String mobile;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(64) DEFAULT NULL COMMENT '用户邮箱'", unique = true)
    public String email;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(64) DEFAULT NULL COMMENT '微信号'", unique = true)
    public String weixinid;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(32) DEFAULT NULL COMMENT '真实姓名'", unique = true)
    public String real_name;

    @DBIndexed
    @Column(columnDefinition = "VARCHAR(18) DEFAULT NULL COMMENT '身份证号码'", unique = true)
    public String id_no;

    @DBIndexed
    @Column(columnDefinition = "TEXT DEFAULT NULL COMMENT '扩展属性'")
    public String ext_attr;
}
