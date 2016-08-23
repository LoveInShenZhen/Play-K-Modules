package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.WeChatMsgBase;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class EventMsgBase extends WeChatMsgBase {

    @Element(data = true)
    public String Event;

}
