package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.WeChatMsgBase;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class NormalMsgBase extends WeChatMsgBase {

    @Element
    public long MsgId;

}
