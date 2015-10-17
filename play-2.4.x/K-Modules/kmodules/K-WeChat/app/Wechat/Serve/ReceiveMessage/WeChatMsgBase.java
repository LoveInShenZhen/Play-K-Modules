package Wechat.Serve.ReceiveMessage;

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
    public String ProcessMessage() {
        return "";
//        return "success";
    }

    @Override
    public void ParseFromStr(String xml_str) {
        super.ParseFromStr(xml_str);
        this.xml_content = xml_str;
    }

}
