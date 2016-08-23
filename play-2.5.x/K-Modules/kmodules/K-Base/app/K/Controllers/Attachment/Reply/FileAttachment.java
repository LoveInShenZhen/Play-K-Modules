package K.Controllers.Attachment.Reply;


import K.Aop.annotations.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import models.K.KAttachment;
import play.data.format.Formats;

import java.util.Date;

/**
 * Created by kk on 15/1/31.
 */
public class FileAttachment {

    @Comment("附件 ID")
    public long id;

    @Comment("文件名称")
    public String file_name;

    @Comment("备注信息")
    public String remarks;

    @Comment("原始大小对应的 ShowUrl")
    public String original_url;

    @Comment("原始大小对应的 ShowUrl")
    public String big_url;

    @Comment("原始大小对应的 ShowUrl")
    public String middle_url;

    @Comment("原始大小对应的 ShowUrl")
    public String small_url;

    @Comment("文件对应的 ContentType, 目前支持:image/JPEG image/PNG")
    public String content_type;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @Comment("上传时间")
    public Date create_time;

    public void LoadBy(KAttachment acat) {
        this.id = acat.id;
        this.file_name = acat.file_name;
        this.remarks = acat.remarks;
        this.original_url = ShowUrl(0);
        this.small_url = ShowUrl(1);
        this.middle_url = ShowUrl(2);
        this.big_url = ShowUrl(3);
        this.content_type = acat.content_type;
        this.create_time = acat.create_time;
    }

    public void LoadBy(long id) {
        KAttachment acat = KAttachment.find.byId(id);
        this.LoadBy(acat);
    }

    // 图片尺寸类型: 0:原始大小 1:缩略图小 2:缩略图中 3:缩略图大
    private String ShowUrl(int size_type) {
        return String.format("/api/Files/Show?id=%d&size_type=%d",
                this.id,
                size_type);
    }

    public String DownloadUrl() {
        return String.format("/api/Files/Download?id=%d", this.id);
    }

}
