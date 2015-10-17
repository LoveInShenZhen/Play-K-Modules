package K.BizLogic;

import models.K.Conf.SysConf;

/**
 * Created by kk on 15/6/11.
 */
public class SysConfBL {

    public static String GetConf(String conf_key, String default_value) {
        SysConf conf = SysConf.find.where()
                .eq("conf_key", conf_key)
                .findUnique();

        if (conf == null) {
            return default_value;
        }

        return conf.conf_value;
    }

    public static String GetExtInfo(String conf_key) {
        SysConf conf = SysConf.find.where()
                .eq("conf_key", conf_key)
                .findUnique();
        if (conf == null) {
            return null;
        }

        return conf.ext_info;
    }

    public static void SetConf(String conf_key, String conf_value, String ext_info) {
        SysConf conf = SysConf.find.where()
                .eq("conf_key", conf_key)
                .findUnique();
        if (conf == null) {
            conf = new SysConf();
        }

        conf.conf_key = conf_key;
        conf.conf_value = conf_value;
        conf.ext_info = ext_info;

        conf.save();
    }

    public static void SetConf(String conf_key, String conf_value) {
        SetConf(conf_key, conf_value, null);
    }
}
