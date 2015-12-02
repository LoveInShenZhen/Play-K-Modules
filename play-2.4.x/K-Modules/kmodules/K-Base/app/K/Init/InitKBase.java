package K.Init;

import K.EventBus.EventBusService;
import K.Service.NotifyService;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import play.Configuration;
import play.Logger;


/**
 * Created by kk on 15/7/13.
 */

public class InitKBase {

    public static void OnStart() {
        Logger.debug("==>  InitKBase.OnStart() ...");

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


    private static boolean RunPlanTaskService() {
        return Configuration.root().getBoolean("K.PlanTaskService", false);
    }

}
