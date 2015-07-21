package controllers;

import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Controllers.ApiDoc.DocUtils;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import play.Logger;
import play.mvc.Result;


/**
 * Created by kk on 15/7/17.
 */

@Comment("临时测试用, 样例")
public class Samples extends JsonpController {

    @Comment("临时测试代码")
    @JsonApi(ReplyClass = ReplyBase.class)
    public Result Test() {
        ReplyBase reply = new ReplyBase();

        Logger.debug("\n{}", DocUtils.GeneratApiMarkdown());

        return ok(reply.ToJsonNode());
    }

}
