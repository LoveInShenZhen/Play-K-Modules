package Wechat.PlanTasks;

import K.BizLogic.SysConfBL;
import Wechat.WeChatApi;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.datetime.JDateTime;
import jodd.util.StringUtil;
import models.K.BgTask.PlanTask;

import java.util.Date;

/**
 * Created by kk on 15/6/11.
 */
public class AutoGetAccessToken implements Runnable {

    public static final String conf_key = "WeChat.AccessToken";

    public static void AddTask(Date plan_run_time) {
        AutoGetAccessToken task = new AutoGetAccessToken();

        PlanTask.addSingletonTask(task,
                "WeChat",
                false,
                plan_run_time,
                "");
    }

    @Override
    public void run() {
        String access_token = SysConfBL.GetConf(conf_key, "");
        if (StringUtil.isBlank(access_token)) {
            // 木有 token, 则获取并更新到 SysConf 里去
            GetNewAccessToken();
        } else {
            // 有已经保存的 AccessToken
            String ext_info = SysConfBL.GetExtInfo(conf_key);
            JDateTime refresh_time = new JDateTime(ext_info, "YYYY-MM-DD hh:mm:ss");
            JDateTime now = new JDateTime();
            now.addSecond(10);
            if (now.isAfter(refresh_time)) {
                // 到期, 强制刷新
                GetNewAccessToken();
            } else {
                // 检查此 AccessToken 是否有效
                if (!WeChatApi.CheckAccessToken(access_token)) {
                    // access_token 无效
                    GetNewAccessToken();
                }
            }
        }

        String ext_info = SysConfBL.GetExtInfo(conf_key);
        JDateTime plan_refresh_time = new JDateTime(ext_info, "YYYY-MM-DD hh:mm:ss");
        AddTask(plan_refresh_time.convertToDate());
    }

    private void GetNewAccessToken() {
        try {
            JsonNode result = WeChatApi.QueryAccessToken();
            if (!WeChatApi.IsSuccessed(result)) {
                // 调用不成功
                AddRetryTask();
            } else {
                // 成功获取新的 access_token
                String access_token = result.get("access_token").textValue();
                int expires_in = result.get("expires_in").intValue();

                JDateTime refresh_time = new JDateTime();
                refresh_time.addSecond(expires_in - 300);

                SysConfBL.SetConf(conf_key, access_token, refresh_time.toString("YYYY-MM-DD hh:mm:ss"));
            }
        } catch (Exception ex) {
            // 例如网络异常, 不合法的 Response
            AddRetryTask();
        }
    }

    private void AddRetryTask() {
        JDateTime retry_time = new JDateTime();
        retry_time.addSecond(10);
        AddTask(retry_time.convertToDate());
    }
}
