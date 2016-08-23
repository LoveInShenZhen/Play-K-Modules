package models.K.EbeanConfig

import K.Common.Helper
import com.avaje.ebean.config.ServerConfig
import com.avaje.ebean.event.ServerConfigStartup

/**
 * Created by kk on 14/11/28.
 */
class MyServerConfigStartup : ServerConfigStartup {
    override fun onStart(serverConfig: ServerConfig) {
        Helper.DLog("He, MyServerConfigStartup is called...")
        serverConfig.encryptKeyManager = MysqlEncryptKeyManager()
    }
}
