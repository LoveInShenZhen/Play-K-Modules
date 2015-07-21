package K.Controllers.ApiDoc.SampleForm;

import K.Aop.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kk on 15/7/17.
 */
public class PostJsonSample {

    @Comment("用户名")
    public String name;

    @Comment("交易记录")
    public List<String> friends;

    public PostJsonSample() {
        friends = new ArrayList<>();
    }
}
