package controllers.Reply;


import K.Aop.annotations.Comment;
import K.Reply.ReplyBase;

/**
 * Created by kk on 15/1/12.
 */
public class LoginReply extends ReplyBase {
    @Comment("登录成功后, 返回登录用户对应的 token")
    public String token;
}
