package K.Common

import java.util.HashSet
import java.util.UUID

/**
 * Created by kk on 14-1-7.
 */
class TokenObject {

    var user_id: UUID? = null

    var roles: Set<Int>

    var platform = ClientType.pc_browser

    init {
        user_id = null
        roles = HashSet<Int>()
    }

    fun HasRole(role: Int): Boolean {

        return roles.contains(role)
    }

    fun JsonString(): String {
        return Helper.ToJsonString(this)
    }

    companion object {

        fun FromJsonString(JsonStr: String): TokenObject? {
            return Helper.FromJsonString(JsonStr, TokenObject::class.java)
        }
    }

}
