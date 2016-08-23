package controllers;

import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.Helper;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import models.SampleModel;
import play.ApplicationLoader;
import play.Logger;
import play.inject.guice.GuiceApplicationLoader;
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

        ApplicationLoader applicationLoader = new GuiceApplicationLoader();
        List<SampleModel> sampleModelList = SampleModel.find.all();

        sampleModelList.stream().forEach(sampleModel -> Logger.debug("==> {}", Helper.ToJsonString(sampleModel)));

        String jstr = "{\n" +
                "  \"id\" : 3,\n" +
                "  \"plan_run_time\" : \"2016-02-21 17:00:00\"\n" +
                "}";

        SampleModel m = Helper.FromJsonString(jstr, SampleModel.class);

        Logger.debug("==> {}", Helper.ToJsonStringPretty(m));

        return ok(reply);
    }

}
