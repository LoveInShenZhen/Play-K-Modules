package Wechat.Serve.ReceiveMessage.Message;

import jodd.util.StringUtil;

/**
 * Created by kk on 15/6/12.
 */
public class EventTypes {

    // 关注/取消关注事件
    public static final String SubscribeEvent = "subscribe";

    // 扫描带参数二维码事件:  简称: 带场景值关注事件
    // 如果用户还未关注公众号，则用户可以关注公众号，关注后微信会将带场景值关注事件推送给开发者
    public static final String SceneSubscribeEvent = "SceneSubscribe";

    // 带场景值扫描事件
    public static final String SceneScanEvent = "SceneScan";

    // 上报地理位置事件
    public static final String LocationEvent = "Location";

    // 点击菜单拉取消息时的事件推送
    public static final String MenuClickEvent = "MenuClick";

    // 点击菜单跳转链接时的事件推送
    public static final String MenuViewEvent = "MenuView";

    public static boolean IsEventMsg(WXMessage message) {
        return message.MsgType.equals("event");
    }

    public static EventMsgBase Analysis(WXMessage message) {

        if (IsSceneScanEvent(message)) {
            SubscribeEvent event = new SubscribeEvent();
            event.ParseFromStr(message.xml_content);

            return event;
        }

        if (IsSceneSubscribeEvent(message)) {

        }

        // 其他未识别的类型
        EventMsgBase msg = new EventMsgBase();
        msg.ParseFromStr(message.xml_content);
        return msg;
    }

    private static boolean IsSubscribeEvent(WXMessage message) {
        if (message.Event.equals("subscribe") || message.Event.equals("unsubscribe")) {
            if (StringUtil.isBlank(message.Ticket)) {
                return true;
            }
        }
        return false;
    }

    private static boolean IsSceneSubscribeEvent(WXMessage message) {
        if (message.Event.equals("subscribe")) {
            if (StringUtil.isNotBlank(message.EventKey) && message.EventKey.startsWith("qrscene_")) {
                return true;
            }
        }

        return false;
    }

    private static boolean IsSceneScanEvent(WXMessage message) {
        if (message.Event.equals("SCAN")) {
            return true;
        }
        return false;
    }

    private static boolean IsLocationEvent(WXMessage message) {
        if (message.Event.equals("LOCATION")) {
            return true;
        }
        return false;
    }

    private static boolean IsMenuClickEvent(WXMessage message) {
        if (message.Event.equals("CLICK")) {
            return true;
        }

        return false;
    }

    private static boolean IsMenuViewEvent(WXMessage message) {
        if (message.Event.equals("VIEW")) {
            return true;
        }

        return false;
    }
}
