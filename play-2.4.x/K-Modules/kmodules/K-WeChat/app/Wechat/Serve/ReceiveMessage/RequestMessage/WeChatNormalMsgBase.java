package Wechat.Serve.ReceiveMessage.RequestMessage;

import Wechat.Serve.ReceiveMessage.WeChatMsgBase;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class WeChatNormalMsgBase extends WeChatMsgBase {

    @Element
    public long MsgID;

}
