package Wechat.ReceiveMessage.ReplyMessage;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class ReplyTextMsg extends ReplyMsgBase {

    @Element(data = true)
    public String Content;

    public ReplyTextMsg() {
        this.MsgType = "text";
    }
}
