package models.K;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-24
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */

import K.Aop.annotations.WithPersistLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@WithPersistLog
@Entity
@Table(name = "k_attachment")
public class KAttachment extends Model {

    @Id
    public long id;

    @Column(columnDefinition = "varchar(256) DEFAULT '' COMMENT '附件材料名称,即扫描件文件名称'")
    public String file_name;

    @Column(columnDefinition = "varchar(4096) DEFAULT '' COMMENT '附件材料文件路径,即文件在文件服务器上的文件路径'")
    public String file_path;

    @Column(columnDefinition = "varchar(64) COMMENT '文件对应的 ContentType, 目前支持:image/JPEG image/PNG'")
    public String content_type;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "timestamp NOT NULL DEFAULT NOW()")
    public Date create_time;

    @Column(columnDefinition = "varchar(4096) DEFAULT '' COMMENT '图片附件大缩略图路径,即文件在文件服务器上的文件路径'")
    public String scale_path_big;

    @Column(columnDefinition = "varchar(4096) DEFAULT '' COMMENT '图片附件中缩略图路径,即文件在文件服务器上的文件路径'")
    public String scale_path_middle;

    @Column(columnDefinition = "varchar(4096) DEFAULT '' COMMENT '图片附件小缩略图路径,即文件在文件服务器上的文件路径'")
    public String scale_path_small;

    @Column(columnDefinition = "TEXT COMMENT '备注信息'")
    public String remarks;

    public static Finder<Long, KAttachment> find = new Finder<>(Long.class, KAttachment.class);

    public boolean IsImage() {
        if (content_type.equalsIgnoreCase("image/jpg")
                || content_type.equalsIgnoreCase("image/png")
                || content_type.equalsIgnoreCase("image/gif")
                || content_type.equalsIgnoreCase("image/jpeg")) {
            return true;
        }
        return false;
    }
}
