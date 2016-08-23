package models.K.EbeanConfig;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;
import play.Logger;

/**
 * Created by kk on 14/11/28.
 */
public class MyServerConfigStartup implements ServerConfigStartup {
    @Override
    public void onStart(ServerConfig serverConfig) {
        Logger.debug("He, MyServerConfigStartup is called...");
        serverConfig.setEncryptKeyManager(new MysqlEncryptKeyManager());
    }
}
