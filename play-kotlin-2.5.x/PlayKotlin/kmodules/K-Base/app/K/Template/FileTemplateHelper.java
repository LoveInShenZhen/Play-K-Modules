package K.Template;

import K.Common.Helper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.exception.ExceptionUtil;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Created by kk on 15/1/7.
 */
public class FileTemplateHelper {

    private static Configuration template_cfg;
//    private static FileTemplateLoader templateLoader;

    static {
        template_cfg = new Configuration(Configuration.VERSION_2_3_21);
        template_cfg.setDefaultEncoding("UTF-8");
        try {
            template_cfg.setDirectoryForTemplateLoading(TemplateDir());
        } catch (IOException e) {
            Logger.error(ExceptionUtil.exceptionChainToString(e));
        }
    }

    private static File TemplateDir() {
        // 约定, 所有的模板文件都放在 /conf/templates 目录下
        return Helper.INSTANCE.getFile("/conf/templates");
    }

    // TemplatePath 为在 /conf/templates 目录下的相对路径
    public static String Process(String TemplatePath, Object data) throws IOException, TemplateException {
        Template template = template_cfg.getTemplate(TemplatePath);
        StringWriter sw = null;
        String result = "";
        try {
            sw = new StringWriter();
            template.process(data, sw);
            result = sw.toString();
            sw.close();
        } finally {
            if (sw != null) sw.close();
        }
        return result;
    }
}
