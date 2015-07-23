package K.Init;

import K.Common.Helper;
import K.EventBus.EventBusService;
import K.Service.NotifyService;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import com.avaje.ebean.Ebean;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;
import jodd.exception.ExceptionUtil;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import play.*;
import play.api.db.DBApi;
import play.db.ebean.EbeanConfig;
import play.db.ebean.ModelsConfigLoader;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kk on 15/7/13.
 */

@Singleton
public class InitKBaseImp implements InitKBase {

    Environment environment;

    Configuration configuration;

    ApplicationLifecycle lifecycle;

    Thread check_application_ready;

    @Inject
    private InitKBaseImp(Environment environment,
                        Configuration configuration,
                        ApplicationLifecycle lifecycle,
                        EbeanConfig config,
                        DBApi dbApi) {

        this.environment = environment;
        this.configuration = configuration;
        this.lifecycle = lifecycle;

        lifecycle.addStopHook(() -> {
            this.OnStop();
            return F.Promise.pure(null);
        });

        check_application_ready = new Thread(() -> {
            while (true) {

                try {
                    Application app = Play.application();
                    if (app != null) {
                        Logger.debug("==>  Application is ready!!!");
                        OnStart();
                        return;
                    }
                } catch (Exception ex) {
                    Helper.SleepInMs(100);
                }
            }
        });

        check_application_ready.start();

    }

    @Override
    public void OnStart() {
        Logger.debug("==>  InitKBase.OnStart() ...");

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
