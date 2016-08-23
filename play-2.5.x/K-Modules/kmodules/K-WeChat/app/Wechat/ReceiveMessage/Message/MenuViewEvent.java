package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.ReplyMessage.ReplyMsgBase;
import Wechat.ReceiveMessage.ReplyMessage.ReplyTextMsg;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import play.Logger;


/**
 * Created by kk on 15/6/12.
 */

/*
 * 点击菜单跳转链接时的事件推送: http://mp.weixin.qq.com/wiki/2/5baf56ce4947d35003b86a9805634b1e.html#.E7.82.B9.E5.87.BB.E8.8F.9C.E5.8D.95.E8.B7.B3.E8.BD.AC.E9.93.BE.E6.8E.A5.E6.97.B6.E7.9A.84.E4.BA.8B.E4.BB.B6.E6.8E.A8.E9.80.81
 */

@Root(name = "xml")
public class MenuViewEvent extends EventMsgBase {

    @Element(data = true)
    public String EventKey;

    @Override
    public ReplyMsgBase ProcessMessage() {
        // todo: 根据 EventKey 来判断被点击的菜单跳转的 url 以便执行相应的逻辑
        Logger.info("MenuViewEvent.EventKey = {}", this.EventKey);

        ReplyTextMsg reply = new ReplyTextMsg();
        reply.ToUserName = this.FromUserName;
        reply.FromUserName = this.ToUserName;
        reply.Content = "";

        return reply;
    }

}
