package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.ReplyMessage.ReplyMsgBase;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/12.
 */

/*
 * 如果用户已经关注公众号，则微信会将带场景值扫描事件推送给开发者。
 * http://mp.weixin.qq.com/wiki/2/5baf56ce4947d35003b86a9805634b1e.html#.E6.89.AB.E6.8F.8F.E5.B8.A6.E5.8F.82.E6.95.B0.E4.BA.8C.E7.BB.B4.E7.A0.81.E4.BA.8B.E4.BB.B6
 */

@Root(name = "xml")
public class SceneScanEvent extends EventMsgBase {

    @Element(data = true)
    public String EventKey;     // 事件KEY值, 是一个32位无符号整数, 即创建二维码时的二维码scene_id

    @Element(data = true)
    public String Ticket;       // 二维码的ticket, 可用来换取二维码图片

    @Override
    public ReplyMsgBase ProcessMessage() {

        // todo:

        return super.ProcessMessage();
    }

}
