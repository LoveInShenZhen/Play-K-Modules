package K;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by kk on 14-1-7.
 */
public class TokenObject {

    public UUID user_id;

    public Set<Integer> roles;

    public String platform = ClientType.pc_browser;

    public TokenObject() {
        user_id = null;
        roles = new HashSet<>();
    }

    public boolean HasRole(int role) {

        return roles.contains(role);
    }

    public String JsonString() {
        return Helper.ToJsonString(this);
    }

    public static TokenObject FromJsonString(String JsonStr) {
        return Helper.FromJsonString(JsonStr, TokenObject.class);
    }

}
