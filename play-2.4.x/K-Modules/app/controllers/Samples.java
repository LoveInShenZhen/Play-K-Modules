package controllers;

import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.Helper;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import models.K.BgTask.PlanTask;
import play.Logger;
import play.mvc.Result;

import java.util.List;

/**
 * Created by kk on 15/7/17.
 */

@Comment("临时测试用, 样例")
public class Samples extends JsonpController {

    @Comment("临时测试代码")
    @JsonApi(ReplyClass = ReplyBase.class)
    public Result Test() {
        ReplyBase reply = new ReplyBase();
        List<PlanTask> tasks = PlanTask.find.all();
        tasks.forEach(planTask -> Logger.debug(Helper.ToJsonStringPretty(planTask)));


        return ok(reply.ToJsonNode());
    }

}
