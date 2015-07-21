package K.Init;

import K.Ebean.DB;
import K.EventBus.EventBusService;
import K.Service.NotifyService;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.db.ebean.EbeanConfig;
import play.inject.ApplicationLifecycle;
import play.libs.F;

/**
 * Created by kk on 15/7/13.
 */

@Singleton
public class InitKBaseImp implements InitKBase {

    Environment environment;

    Configuration configuration;

    ApplicationLifecycle lifecycle;

    @Inject
    public InitKBaseImp(Environment environment,
                        Configuration configuration,
                        ApplicationLifecycle lifecycle) {

        this.environment = environment;
        this.configuration = configuration;
        this.lifecycle = lifecycle;

        lifecycle.addStopHook(() -> {
            this.OnStop();
            return F.Promise.pure(null);
        });

        this.OnStart();

    }

    @Override
    public void OnStart() {
        Logger.debug("==> InitKBase.OnStart() ...");

        if (RunPlanTaskService()) {
            PlanTaskService service = new PlanTaskService(this.configuration);
            ServiceMgr.Instance.RegService(service);

            AddServiceTasks();

            EventBusService event_bus_srv = new EventBusService(this.configuration);
            ServiceMgr.Instance.RegService(event_bus_srv);
        }

        NotifyService notifyService = new NotifyService(this.configuration);
        ServiceMgr.Instance.RegService(notifyService);

        ServiceMgr.Instance.StartAll();
    }

    @Override
    public void OnStop() {
        Logger.debug("==> InitKBase.OnStop ...");
        ServiceMgr.Instance.StopAll();
    }

    @Override
    public Configuration Config() {
        return this.configuration;
    }

    private boolean RunPlanTaskService() {
        return this.configuration.getBoolean("K.PlanTaskService", false);
    }

    private void AddServiceTasks() {
        // todo
    }

}
