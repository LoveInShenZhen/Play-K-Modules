package K.BizLogic

import models.K.Conf.SysConf

/**
 * Created by kk on 15/6/11.
 */
object SysConfBL {

    fun GetConf(conf_key: String, default_value: String): String {
        val conf = SysConf.find.where()
                .eq("conf_key", conf_key)
                .findUnique()

        if (conf == null) {
            return default_value
        } else {
            return conf.conf_value ?: "";
        }
    }

    fun GetExtInfo(conf_key: String): String? {
        val conf = SysConf.find.where().eq("conf_key", conf_key).findUnique() ?: return null

        return conf.ext_info
    }

    @JvmOverloads fun SetConf(conf_key: String, conf_value: String, ext_info: String? = null) {
        var conf = SysConf.find.where().eq("conf_key", conf_key).findUnique()
        if (conf == null) {
            conf = SysConf()
        }

        conf.conf_key = conf_key
        conf.conf_value = conf_value
        conf.ext_info = ext_info

        conf.save()
    }
}
