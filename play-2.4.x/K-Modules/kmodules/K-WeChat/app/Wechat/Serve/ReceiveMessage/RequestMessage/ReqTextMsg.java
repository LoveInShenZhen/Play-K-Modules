package Wechat.Serve.ReceiveMessage.RequestMessage;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

@Root(name = "xml")
public class ReqTextMsg extends WeChatNormalMsgBase {

    @Element(data=true)
    public String Content;

}
