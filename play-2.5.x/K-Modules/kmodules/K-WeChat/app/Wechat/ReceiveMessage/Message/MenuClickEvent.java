package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.ReplyMessage.ReplyMsgBase;
import Wechat.ReceiveMessage.ReplyMessage.ReplyTextMsg;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


/**
 * Created by kk on 15/6/12.
 */

/*
 * 点击菜单拉取消息时的事件推送: http://mp.weixin.qq.com/wiki/2/5baf56ce4947d35003b86a9805634b1e.html#.E7.82.B9.E5.87.BB.E8.8F.9C.E5.8D.95.E6.8B.89.E5.8F.96.E6.B6.88.E6.81.AF.E6.97.B6.E7.9A.84.E4.BA.8B.E4.BB.B6.E6.8E.A8.E9.80.81
 */

@Root(name = "xml")
public class MenuClickEvent extends EventMsgBase {

    @Element(data = true)
    public String EventKey;

    @Override
    public ReplyMsgBase ProcessMessage() {
        ReplyTextMsg reply = new ReplyTextMsg();
        reply.ToUserName = this.FromUserName;
        reply.FromUserName = this.ToUserName;
        reply.Content = "success";

        return reply;
    }
}
