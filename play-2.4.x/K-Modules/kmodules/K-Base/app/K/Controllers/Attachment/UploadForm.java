package K.Controllers.Attachment;


import K.BizLogicException;
import K.Images;
import K.DB;
import K.Aop.annotations.Comment;
import jodd.io.FileNameUtil;
import jodd.io.FileUtil;
import jodd.util.SystemUtil;
import models.K.KAttachment;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

/**
 * Created by kk on 15/1/31.
 */
public class UploadForm {

    private final static long max_file_size = 1024 * 1024 * 2;

    private final static int scale_big_size = 1024;

    private final static int scale_middle_size = 480;

    private final static int scale_small_size = 120;

    private final static String file_field_name = "attachmentFile";

    @Comment("是否要求加水印, 仅对图片类文件有效")
    public boolean add_watermark = true;

    @Comment("备注信息")
    public String remarks = "";

    public boolean IsImageFile() {
        Http.MultipartFormData.FilePart filePart = GetFilePart();
        String fileNameExt = FileNameUtil.getExtension(filePart.getFilename());
        if (fileNameExt.equalsIgnoreCase("jpg") || fileNameExt.equalsIgnoreCase("gif")
                || fileNameExt.equalsIgnoreCase("png") || fileNameExt.equalsIgnoreCase("jpeg")
                || fileNameExt.equalsIgnoreCase("bmp")) {
            return true;
        }
        return false;
    }

    public String ImageFormat() {
        String content_type = this.GetFilePart().getContentType();
        return StringUtils.split(content_type, "/")[1];
    }

    private Http.MultipartFormData.FilePart GetFilePart() {
        Http.MultipartFormData body = Controller.request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart filePart = body.getFile(UploadForm.file_field_name);
        return filePart;
    }

    private String WarteRmarkPicPath() {
        return Configuration.root().getString("K.WarteRmarkPicPath", "/conf/resource/wartermark.png");
    }

    private String WaterPath() {
        return Play.application().getFile(WarteRmarkPicPath()).getAbsolutePath();
    }

    public long SaveAttachment() throws IOException {
        DynamicForm post_data = Form.form().bindFromRequest();
        if (StringUtils.isNotBlank(post_data.get("remarks"))) {
            this.remarks = post_data.get("remarks");
        }
        if (StringUtils.isNotBlank(post_data.get("add_watermark"))) {
            this.add_watermark = Boolean.parseBoolean(post_data.get("add_watermark"));
        }

        Http.MultipartFormData.FilePart filePart = GetFilePart();
        long file_size = org.apache.commons.io.FileUtils.sizeOf(filePart.getFile());
        Logger.debug("==> file_size = {}", file_size);
        if (file_size > max_file_size) {
            throw new BizLogicException("上传文件不能超过 2 MB");
        }

        if (IsImageFile()) {
            return SaveImageTypeFile();
        } else {
            return SaveOthetTypeFile();
        }
    }

    public static String GetUploadDir() throws IOException {
        String upload_dir = "";
        String cfg = Configuration.root().getString("file_upload_dir", "");
        if (StringUtils.isBlank(cfg)) {
            // 如果没有指定, 则设定此目录为用户 Home 目录下创建的 /data/pnr-p2p/upload_file 目录
            String user_home_dir = SystemUtil.getUserHome();
            upload_dir = FileNameUtil.concat(user_home_dir, "data/pnr-p2p/upload_file");
        } else {
            upload_dir = cfg;
        }
        FileUtil.mkdirs(upload_dir);
        return upload_dir;
    }

    private String GetSaveDirPath() throws IOException {
        String upload_dir = GetUploadDir();
        for (int level_1 = 0; level_1 < 4096; level_1++) {
            for (int level_2 = 0; level_2 < 4096; level_2++) {
                String dest_dir_path = Paths.get(upload_dir, Integer.toString(level_1), Integer.toString(level_2)).toString();
                FileUtil.mkdirs(dest_dir_path);
                File dir = new File(dest_dir_path);
                int file_counts = dir.list().length;
                if (file_counts > 2048) {
                    continue;
                }
                return dest_dir_path;
            }
        }
        return upload_dir;
    }

    private long SaveImageTypeFile() throws IOException {
        Http.MultipartFormData.FilePart filePart = GetFilePart();
        String save_dir_path = GetSaveDirPath();
        String src_fpath = filePart.getFile().getAbsolutePath();
        String dest_fpath = FileNameUtil.concat(save_dir_path, UUID.randomUUID().toString());


        KAttachment ac = new KAttachment();
        ac.file_name = filePart.getFilename();
        ac.file_path = dest_fpath;
        ac.content_type = filePart.getContentType();
        ac.create_time = new Date();
        ac.remarks = this.remarks;

        if (this.add_watermark) {
            // 图片要求加水印
            File watermarkImageFile = new File(WaterPath());
            File original_file = new File(src_fpath);
            File dest_file = new File(dest_fpath);
            Images.addImageWatermark(watermarkImageFile, original_file, dest_file, this.ImageFormat(), 0.4f);

            ac.scale_path_big = ReSize(dest_fpath, scale_big_size);
            ac.scale_path_middle = ReSize(dest_fpath, scale_middle_size);
            ac.scale_path_small = ReSize(dest_fpath, scale_small_size);

        } else {
            FileUtil.copy(src_fpath, dest_fpath);
            ac.scale_path_big = dest_fpath;
            ac.scale_path_middle = dest_fpath;
            ac.scale_path_small = dest_fpath;
        }

        FileUtil.delete(src_fpath);

        DB.ReadWriteDB().save(ac);
        return ac.id;
    }

    private String ReSize(String fpath, int scale_size) throws IOException {
        File originalImage = new File(fpath);
        BufferedImage img = ImageIO.read(originalImage);
        Rectangle new_size = calculateScaleSize(img, scale_size);
        if (new_size != null) {
            String new_fpath = String.format("%s.%d", fpath, scale_size);
            File new_file = new File(new_fpath);
            Images.resize(originalImage, new_file, new_size.width, new_size.height);
            return new_fpath;
        } else {
            return fpath;
        }
    }

    private Rectangle calculateScaleSize(BufferedImage img, int scale_size) {
        int width = img.getWidth();
        int height = img.getHeight();

        if (width > height) {
            if (width < scale_size) {
                return null;
            }
            int scale_width = scale_size;
            int scale_height = scale_width * height / width;
            return new Rectangle(scale_width, scale_height);
        } else {
            if (height < scale_size) {
                return null;
            }
            int scale_height = scale_size;
            int scale_width = scale_height * width / height;
            return new Rectangle(scale_width, scale_height);
        }
    }

    private long SaveOthetTypeFile() throws IOException {
        Http.MultipartFormData.FilePart filePart = GetFilePart();
        String save_dir_path = GetSaveDirPath();
        String dest_fpath = FileNameUtil.concat(save_dir_path, UUID.randomUUID().toString());
        String src_fpath = filePart.getFile().getAbsolutePath();

        KAttachment ac = new KAttachment();
        ac.file_name = filePart.getFilename();
        ac.file_path = dest_fpath;
        ac.content_type = filePart.getContentType();
        ac.create_time = new Date();
        ac.scale_path_big = dest_fpath;
        ac.scale_path_middle = dest_fpath;
        ac.scale_path_small = dest_fpath;
        ac.remarks = this.remarks;

        FileUtil.move(src_fpath, dest_fpath);

        DB.ReadWriteDB().save(ac);

        return ac.id;
    }

}
