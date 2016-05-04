package Wechat.ReceiveMessage.Message;

import Wechat.ReceiveMessage.WeChatMsgBase;
import jodd.util.StringUtil;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import play.Logger;


/**
 * Created by kk on 15/6/12.
 */

@Root(name = "xml")
public class WXMessage extends WeChatMsgBase {

    @Element(required = false)
    public Long MsgID;

    @Element(data = true, required = false)
    public String Event;

    @Element(data = true, required = false)
    public String EventKey;

    @Element(data = true, required = false)
    public String Ticket;

    @Element(data = true, required = false)
    public String Latitude;

    @Element(data = true, required = false)
    public String Longitude;

    @Element(data = true, required = false)
    public String Precision;

    @Element(data = true, required = false)
    public String Content;

    @Element(data = true, required = false)
    public String PicUrl;

    @Element(data = true, required = false)
    public String MediaId;

    @Element(data = true, required = false)
    public String Format;

    @Element(data = true, required = false)
    public String ThumbMediaId;

    @Element(required = false)
    public Double Location_X;

    @Element(required = false)
    public Double Location_Y;

    @Element(required = false)
    public Integer Scale;

    @Element(data = true, required = false)
    public String Label;

    @Element(data = true, required = false)
    public String Title;

    @Element(data = true, required = false)
    public String Description;

    @Element(data = true, required = false)
    public String Url;

    @Element(data = true, required = false)
    public String Recognition;

    public WeChatMsgBase CreateByMsgType() {

        Logger.info("xml_content:\n{}", this.xml_content);

        if (this.MsgType.equals("text")) {
            TextMsg msg = new TextMsg();
            msg.ParseFromStr(this.xml_content);
            return msg;
        }

        if (this.MsgType.equals("event")) {
            if (this.Event.equals("subscribe") || this.Event.equals("unsubscribe")) {
                if (StringUtil.isBlank(this.EventKey)) {
                    SubscribeEvent msg = new SubscribeEvent();
                    msg.ParseFromStr(this.xml_content);
                    return msg;
                }

                if (this.EventKey.startsWith("qrscene_")) {
                    SceneSubscribeEvent msg = new SceneSubscribeEvent();
                    msg.ParseFromStr(this.xml_content);
                    return msg;
                }
            }

            if (this.Event.equals("SCAN")) {
                SceneScanEvent msg = new SceneScanEvent();
                msg.ParseFromStr(this.xml_content);
                return msg;
            }

            if (this.Event.equals("CLICK")) {
                MenuClickEvent msg = new MenuClickEvent();
                msg.ParseFromStr(this.xml_content);
                return msg;
            }

            if (this.Event.equals("VIEW")) {
                MenuViewEvent msg = new MenuViewEvent();
                msg.ParseFromStr(this.xml_content);
                return msg;
            }
        }

        return this;
    }


}
