package K.Template;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kk on 15/7/10.
 */
public class ResourceTemplateHelper {

    private static Map<String, Configuration> jar_to_confgs;

    static {
        jar_to_confgs = new HashMap<>();
    }

    private static String JarPathBy(Class clazz) {
        try {
            return clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        } catch (Exception ex) {
            return "";
        }
    }

    private static Configuration GetConfigBy(Class clazz) {
        String jar_path = JarPathBy(clazz);
        if (jar_to_confgs.containsKey(jar_path)) {
            return jar_to_confgs.get(jar_path);
        } else {
            Configuration config = new Configuration(Configuration.VERSION_2_3_21);
            config.setDefaultEncoding("UTF-8");
            ClassTemplateLoader loader = new ClassTemplateLoader(clazz, "/");
            config.setTemplateLoader(loader);

            jar_to_confgs.put(jar_path, config);

            return config;
        }
    }

    public static String Process(Class clazz, String template_path, Object data) throws IOException, TemplateException {

        StringWriter sw = null;
        String result = "";
        try {
            Configuration config = GetConfigBy(clazz);
            Template template = config.getTemplate(template_path);
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
