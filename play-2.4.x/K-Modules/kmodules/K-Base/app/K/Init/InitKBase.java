package K.Init;

import K.Common.JDateTimeJsonDeserializer;
import K.Common.JDateTimeJsonSerializer;
import K.EventBus.EventBusService;
import K.Service.NotifyService;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jodd.datetime.JDateTime;
import play.Configuration;
import play.Logger;
import play.libs.Json;


/**
 * Created by kk on 15/7/13.
 */

public class InitKBase {

    public static void OnStart() {
        Logger.debug("==>  InitKBase.OnStart() ...");

        RegistJacksonModule();

        if (RunPlanTaskService()) {

            PlanTaskService service = new PlanTaskService();
            ServiceMgr.Instance.RegService(service);

            EventBusService event_bus_srv = EventBusService.Singleton();
            ServiceMgr.Instance.RegService(event_bus_srv);
        }

        NotifyService notifyService = new NotifyService(Configuration.root());
        ServiceMgr.Instance.RegService(notifyService);

        ServiceMgr.Instance.StartAll();

    }

    public static void OnStop() {
        Logger.debug("==> InitKBase.OnStop ...");
        ServiceMgr.Instance.StopAll();
    }

    private static void RegistJacksonModule() {
        SimpleModule module = new SimpleModule("CustomTypeModule");
        module.addSerializer(JDateTime.class, new JDateTimeJsonSerializer());
        module.addDeserializer(JDateTime.class, new JDateTimeJsonDeserializer());

        Json.mapper().registerModule(module);
    }

    private static boolean RunPlanTaskService() {
        return Configuration.root().getBoolean("K.PlanTaskService", false);
    }

}
