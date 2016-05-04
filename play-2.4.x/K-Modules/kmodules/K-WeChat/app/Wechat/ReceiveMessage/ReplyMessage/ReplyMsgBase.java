package Wechat.ReceiveMessage.ReplyMessage;

import Wechat.XmlBeanBase;
import jodd.datetime.JDateTime;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/12.
 */

@Root(name = "xml")
public class ReplyMsgBase extends XmlBeanBase {

    @Element(data=true)
    public String ToUserName;

    @Element(data=true)
    public String FromUserName;

    @Element
    public long CreateTime = new JDateTime().getTimeInMillis()/1000;

    @Element(data=true)
    public String MsgType;

}
