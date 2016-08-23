package K.AppModules.Component;

import K.Common.Helper;
import K.Common.Hub;
import K.Common.JDateTimeJsonDeserializer;
import K.Common.JDateTimeJsonSerializer;
import K.EventBus.EventBusService;
import K.Service.NotifyService;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jodd.datetime.JDateTime;
import play.Application;
import play.Configuration;
import play.Environment;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.inject.ApplicationLifecycle;
import play.libs.Json;
import play.routing.Router;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

/**
 * Created by kk on 16/5/5.
 */

@Singleton
public class OnKBaseStartStop {

    @Inject
    public OnKBaseStartStop(ApplicationLifecycle applicationLifecycle,
                            Environment environment,
                            Application application,
                            Configuration configuration,
                            CacheApi cacheApi,
                            FormFactory formFactory,
                            Provider<Router> routerProvider) {
        // 添加 EbeanConfig 依赖, 确保在此之前, Ebean 已经正常初始化
        Hub.setEnvironment(environment);
        Hub.setApplication(application);
        Hub.setConfiguration(configuration);
        Hub.setCacheApi(cacheApi);
        Hub.setFormFactory(formFactory);
        Hub.setRouterProvider(routerProvider);

        applicationLifecycle.addStopHook(() -> {
            OnStop();
            return CompletableFuture.completedFuture(null);
        });

        OnStart();
    }

    private void OnStart() {
        Helper.DLog("==> KBase OnStart() ...");
        RegistJacksonModule();

        if (RunPlanTaskService()) {
            PlanTaskService service = new PlanTaskService();
            ServiceMgr.Instance.RegService(service);
        }

        NotifyService notifyService = new NotifyService();
        ServiceMgr.Instance.RegService(notifyService);

        EventBusService event_bus_srv = EventBusService.Singleton();
        ServiceMgr.Instance.RegService(event_bus_srv);

        ServiceMgr.Instance.StartAll();
    }

    private void OnStop() {
        Helper.DLog("==> KBase OnStop() ...");
        ServiceMgr.Instance.StopAll();
    }

    private void RegistJacksonModule() {
        SimpleModule module = new SimpleModule("CustomTypeModule");
        module.addSerializer(JDateTime.class, new JDateTimeJsonSerializer());
        module.addDeserializer(JDateTime.class, new JDateTimeJsonDeserializer());

        Json.mapper().registerModule(module);
    }

    private static boolean RunPlanTaskService() {
        return Hub.configuration().getBoolean("K.PlanTaskService", false);
    }
}