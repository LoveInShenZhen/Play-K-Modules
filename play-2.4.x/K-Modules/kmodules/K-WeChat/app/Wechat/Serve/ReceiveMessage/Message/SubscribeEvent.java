package Wechat.Serve.ReceiveMessage.Message;

import org.simpleframework.xml.Root;
import play.Logger;


/**
 * Created by kk on 15/6/10.
 */

/*
 * 关注/取消关注事件: http://mp.weixin.qq.com/wiki/2/5baf56ce4947d35003b86a9805634b1e.html#.E5.85.B3.E6.B3.A8.2F.E5.8F.96.E6.B6.88.E5.85.B3.E6.B3.A8.E4.BA.8B.E4.BB.B6
 */

@Root(name = "xml")
public class SubscribeEvent extends EventMsgBase {

    @Override
    public String ProcessMessage() {

        if (this.Event.equals("subscribe")) {
            return OnSubscribe();
        } else {
            return OnUnsubscribe();
        }

    }

    // 关注
    private String OnSubscribe() {
        // todo
        Logger.info("==> 有人关注: open_id: {}", this.FromUserName);

//        User user = UserBL.GetUserByWeChat(this.FromUserName);
//        if (user != null) {
//            user.wechat_open_id = null;
//            DB.ReadWriteDB().save(user);
//        }

        return "";
    }

    // 取消关注
    private String OnUnsubscribe() {

        // todo
        Logger.info("==> 有人取消关注: open_id: {}", this.FromUserName);

//        User user = UserBL.GetUserByWeChat(this.FromUserName);
//
//        if (user != null) {
//            user.wechat_open_id = null;
//            DB.ReadWriteDB().save(user);
//        }

        return "";
    }

}
