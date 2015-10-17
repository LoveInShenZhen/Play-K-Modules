package Wechat.Serve.ReceiveMessage.Message;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by kk on 15/6/10.
 */

/*
 * 接收普通消息-文本消息: http://mp.weixin.qq.com/wiki/10/79502792eef98d6e0c6e1739da387346.html#.E6.96.87.E6.9C.AC.E6.B6.88.E6.81.AF
 */

@Root(name = "xml")
public class TextMsg extends NormalMsgBase {

    @Element(data=true)
    public String Content;

    @Override
    public String ProcessMessage() {

        // todo: 根据 Content 来响应不同的消息给用户

        return "Hello World " + Content;
    }
}
