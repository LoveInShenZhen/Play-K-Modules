package models.K

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-24
 * Time: 下午11:29
 * To change this template use File | Settings | File Templates.
 */

import K.Aop.annotations.WithPersistLog
import com.avaje.ebean.Model
import com.fasterxml.jackson.annotation.JsonFormat
import play.data.format.Formats

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import java.util.Date

@WithPersistLog
@Entity
@Table(name = "k_attachment")
class KAttachment : Model() {

    @Id
    var id: Long = 0

    @Column(columnDefinition = "VARCHAR(256) DEFAULT '' COMMENT '附件材料名称,即扫描件文件名称'")
    var file_name: String? = null

    @Column(columnDefinition = "VARCHAR(4096) DEFAULT '' COMMENT '附件材料文件路径,即文件在文件服务器上的文件路径'")
    var file_path: String? = null

    @Column(columnDefinition = "VARCHAR(64) COMMENT '文件对应的 ContentType, 目前支持:image/JPEG image/PNG'")
    var content_type: String? = null

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Column(columnDefinition = "TIMESTAMP NOT NULL DEFAULT NOW()")
    var create_time: Date? = null

    @Column(columnDefinition = "VARCHAR(4096) DEFAULT '' COMMENT '图片附件大缩略图路径,即文件在文件服务器上的文件路径'")
    var scale_path_big: String? = null

    @Column(columnDefinition = "VARCHAR(4096) DEFAULT '' COMMENT '图片附件中缩略图路径,即文件在文件服务器上的文件路径'")
    var scale_path_middle: String? = null

    @Column(columnDefinition = "VARCHAR(4096) DEFAULT '' COMMENT '图片附件小缩略图路径,即文件在文件服务器上的文件路径'")
    var scale_path_small: String? = null

    @Column(columnDefinition = "TEXT COMMENT '备注信息'")
    var remarks: String? = null


    fun IsImage(): Boolean {
        if (content_type!!.equals("image/jpg", ignoreCase = true)
                || content_type!!.equals("image/png", ignoreCase = true)
                || content_type!!.equals("image/gif", ignoreCase = true)
                || content_type!!.equals("image/jpeg", ignoreCase = true)) {
            return true
        }
        return false
    }

    companion object {

        var find = Model.Finder<Long, KAttachment>(KAttachment::class.java)
    }
}
