package K.Controllers.ApiDoc.Reply;


import K.Reply.ReplyBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 14/11/5.
 */
public class ApiList extends ReplyBase {
    public List<ApiInfo> api_list;

    public ApiList() {
        api_list = new ArrayList<>();
    }
}
