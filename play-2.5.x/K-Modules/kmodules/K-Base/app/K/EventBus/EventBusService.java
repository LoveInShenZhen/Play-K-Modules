package K.EventBus;

import K.Common.BizLogicException;
import K.Common.Helper;
import K.Common.Hub;
import K.Service.ServiceBase;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.ClassPath;
import jodd.exception.ExceptionUtil;
import play.Configuration;
import play.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 14/11/8.
 */

public class EventBusService extends ServiceBase {


    private EventBus sync_event_bus;
    private EventBus async_event_bus;
    private ExecutorService async_event_bus_executor;


    private static EventBusService singleton_service;

    static {
        singleton_service = new EventBusService();
    }

    public static EventBusService Singleton() {
        return singleton_service;
    }

    private EventBusService() {
        super("EventBusService");
    }

    @Override
    public void Start() {
        sync_event_bus = new EventBus(new ExceptionHandler());
        async_event_bus_executor = Executors.newCachedThreadPool();
        async_event_bus = new AsyncEventBus(async_event_bus_executor, new ExceptionHandler());
        ScanAndRegHandlers();
        setRunning(true);
    }

    @Override
    public boolean Stop() {
        try {
            setRunning(false);
            async_event_bus_executor.shutdown();
            async_event_bus_executor.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.warn(ExceptionUtil.exceptionChainToString(e));
        }

        return true;
    }

    private EventBus SyncEventBus() {
        return sync_event_bus;
    }

    private EventBus AsyncEventBus() {
        return async_event_bus;
    }

    private void PostSync(Object event) {
        this.sync_event_bus.post(event);
    }

    private void PostAsync(Objects event) {
        this.async_event_bus.post(event);
    }

    private void regSyncEventHandler(String className) throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException {
        Class cls = Helper.LoadClass(className);

        Object handler = cls.newInstance();
        this.sync_event_bus.register(handler);
    }

    private void regAsyncEventHandler(String className) throws ClassNotFoundException,
            IllegalAccessException,
            InstantiationException {
        Class cls = Helper.LoadClass(className);

        Object handler = cls.newInstance();
        this.async_event_bus.register(handler);
    }

    private void regEventHanlderFromPackage(boolean is_sync_handler, String package_name) {
        try {
            ClassPath cp = ClassPath.from(EventBusService.class.getClassLoader());
            ImmutableSet<ClassPath.ClassInfo> classes = cp.getTopLevelClassesRecursive(package_name);
            for (ClassPath.ClassInfo c : classes) {
                Class cls = c.load();
                Object handler = cls.newInstance();
                if (is_sync_handler) {
                    Logger.debug("Reg sync event handler: {}", handler.getClass().getName());
                    this.sync_event_bus.register(handler);
                } else {
                    Logger.debug("Reg Async event handler: {}", handler.getClass().getName());
                    this.async_event_bus.register(handler);
                }

            }

        } catch (Exception ex) {
            Logger.error("注册 EventHandler 失败: \n{}", ExceptionUtil.exceptionChainToString(ex));
        }
    }

    private List<String> SyncHanldersPackages() {
        return Hub.configuration().getStringList("EventBus.SyncHanlders", Arrays.asList("K.EventBus.Handlers.SyncHanlders"));
    }

    private List<String> AsyncHandlersPackages() {
        return Hub.configuration().getStringList("EventBus.AsyncHandlers", Arrays.asList("K.EventBus.Handlers.AsyncHandlers"));
    }

    private void CheckHanldersPackages() {
        List<String> async_paths = AsyncHandlersPackages();
        for (String path: SyncHanldersPackages()) {
            if (async_paths.contains(path)) {
                throw new BizLogicException("%s 不能同时是 SyncHanlders 和 AsyncHandlers 的 package");
            }
        }
    }

    private void ScanAndRegHandlers() {
        CheckHanldersPackages();

        for (String path: SyncHanldersPackages()) {
            regEventHanlderFromPackage(true, path);
        }

        for (String path: AsyncHandlersPackages()) {
            regEventHanlderFromPackage(false, path);
        }
    }

    // static methods
    public static void PostSyncEvent(Object event) {
        singleton_service.sync_event_bus.post(event);
    }

    public static void PostAsyncEvent(Object event) {
        singleton_service.async_event_bus.post(event);
    }

}
