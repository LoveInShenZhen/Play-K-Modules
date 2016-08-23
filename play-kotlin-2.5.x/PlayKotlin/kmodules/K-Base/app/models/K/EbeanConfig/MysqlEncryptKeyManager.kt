package models.K.EbeanConfig

import K.Common.Helper
import com.avaje.ebean.config.EncryptKey
import com.avaje.ebean.config.EncryptKeyManager

class MysqlEncryptKeyManager : EncryptKeyManager {

    init {
        Helper.DLog("Ebean Server load MysqlEncryptKeyManager")
    }

    override fun getEncryptKey(tableName: String, columnName: String): EncryptKey {

        return MysqlEncrptKey(tableName, columnName)
    }

    override fun initialise() {

    }

    internal inner class MysqlEncrptKey(private val tableName: String, private val columnName: String) : EncryptKey {

        override fun getStringValue(): String {
            val sv = Helper.Md5OfString(String.format("%s@%s", tableName, columnName))
            return sv
        }

    }
}
