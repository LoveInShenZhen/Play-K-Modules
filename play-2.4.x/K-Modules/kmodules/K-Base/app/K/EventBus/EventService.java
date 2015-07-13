package K.EventBus;

import K.Helper;
import K.Service.ServiceBase;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.ClassPath;
import jodd.exception.ExceptionUtil;
import play.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 14/11/8.
 */
public class EventService extends ServiceBase {


    private EventBus sync_event_bus;
    private EventBus async_event_bus;
    private ExecutorService async_event_bus_executor;


    private static EventService singleton_service;

    static {
        singleton_service = new EventService();
    }

    public static EventService Singleton() {
        return singleton_service;
    }

    private EventService() {
        super("EventBusService");
        sync_event_bus = new EventBus(new ExceptionHandler());
        async_event_bus_executor = Executors.newCachedThreadPool();
        async_event_bus = new AsyncEventBus(async_event_bus_executor, new ExceptionHandler());
        regEventHanlderFromPackage(true, "pnrp2p.EventBus.Handlers.SyncHanlders");
        regEventHanlderFromPackage(false, "pnrp2p.EventBus.Handlers.AsyncHandlers");
        setRunning(true);
    }

    @Override
    public void Start() {
        // EventService 作为基础服务, 必然要启动的
    }

    @Override
    public boolean Stop() {
        try {
            async_event_bus_executor.shutdown();
            async_event_bus_executor.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.warn(ExceptionUtil.exceptionChainToString(e));
        }
        setRunning(false);
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
        regEventHanlderFromPackage(false, "pnrp2p.EventBus.Handlers.AsyncHandlers");
        regEventHanlderFromPackage(true, "pnrp2p.EventBus.Handlers.SyncHanlders");
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
            ClassPath cp = ClassPath.from(EventService.class.getClassLoader());
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

    // static methods
    public static void PostSyncEvent(Object event) {
        singleton_service.sync_event_bus.post(event);
    }

    public static void PostAsyncEvent(Object event) {
        singleton_service.async_event_bus.post(event);
    }

}
