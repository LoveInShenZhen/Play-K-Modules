package models.K.EbeanConfig;

import K.Common.Helper;
import com.avaje.ebean.config.EncryptKey;
import com.avaje.ebean.config.EncryptKeyManager;
import play.Logger;

public class MysqlEncryptKeyManager implements EncryptKeyManager {

    public MysqlEncryptKeyManager() {
        Logger.debug("Ebean Server load MysqlEncryptKeyManager");
    }

    @Override
    public EncryptKey getEncryptKey(String tableName, String columnName) {

        return new MysqlEncrptKey(tableName, columnName);
    }

    @Override
    public void initialise() {

    }

    class MysqlEncrptKey implements EncryptKey {

        private String tableName;
        private String columnName;

        public MysqlEncrptKey(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        @Override
        public String getStringValue() {
            String sv = Helper.Md5OfString(String.format("%s@%s", tableName, columnName));
            return sv;
        }

    }
}
