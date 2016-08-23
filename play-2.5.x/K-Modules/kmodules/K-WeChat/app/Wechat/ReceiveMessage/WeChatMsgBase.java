package Wechat.ReceiveMessage;

import Wechat.ReceiveMessage.ReplyMessage.ReplyMsgBase;
import Wechat.ReceiveMessage.ReplyMessage.ReplyTextMsg;
import Wechat.XmlBeanBase;
import jodd.datetime.JDateTime;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class WeChatMsgBase extends XmlBeanBase {

    @Element(data = true)
    public String ToUserName;

    @Element(data = true)
    public String FromUserName;

    @Element
    public long CreateTime = new JDateTime().getTimeInMillis() / 1000;

    @Element(data = true)
    public String MsgType;

    public String xml_content;

    // 子类针对自己的消息类型, 重写此方法
    public ReplyMsgBase ProcessMessage() {
        ReplyTextMsg reply = new ReplyTextMsg();
        reply.ToUserName = this.FromUserName;
        reply.FromUserName = this.ToUserName;
        reply.Content = "";

        return reply;
    }

    @Override
    public void ParseFromStr(String xml_str) {
        super.ParseFromStr(xml_str);
        this.xml_content = xml_str;
    }

}
