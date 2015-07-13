package K.Controllers.Attachment;


import K.Aop.annotations.Comment;
import K.Controllers.JsonpController;
import models.K.KAttachment;
import play.Play;
import play.mvc.Http;
import play.mvc.Result;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by kk on 15/1/14.
 */
public class FileAttachments extends JsonpController {

    @Comment("根据附件类型,显示附件")
    public static Result ShowAttachement(@Comment("图片附件对应的 id") long id,
                                         @Comment("图片尺寸类型: 0:原始大小 1:缩略图小 2:缩略图中 3:缩略图大") int size_type)
            throws FileNotFoundException {
        KAttachment ac = KAttachment.find.byId(id);

        if (ac == null) {
            return show404Image();
        }

        String fpath = "";
        if (size_type == 0) {
            fpath = ac.file_path;
        }
        if (size_type == 1) {
            fpath = ac.scale_path_small;
        }
        if (size_type == 2) {
            fpath = ac.scale_path_middle;
        }
        if (size_type == 3) {
            fpath = ac.scale_path_big;
        }

        File imgf = new File(fpath);
        if (!imgf.exists() || !imgf.canRead()) {
            return show404Image();
        }
        FileInputStream fs = new FileInputStream(imgf);
        return ok(fs).as(ac.content_type);
    }

    public static Result show404Image() throws FileNotFoundException {
        String img404_path = Play.application().getFile("/conf/resource/404.jpg").getAbsolutePath();
        FileInputStream fs = new FileInputStream(img404_path);
        response().setHeader(CACHE_CONTROL, "max-age=8640000");
        return ok(fs).as("image/jpg");
    }

    @Comment("下载附件")
    public static Result Download(@Comment("图片附件对应的 id") long id) throws FileNotFoundException {
        KAttachment ac = KAttachment.find.byId(id);

        if (ac == null) {
            return show404Image();
        }
        File imgf = new File(ac.file_path);
        if (!imgf.exists() || !imgf.canRead()) {
            return show404Image();
        }

        Http.Response response = response();
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", ac.file_name));
        return ok(imgf);
    }

}
