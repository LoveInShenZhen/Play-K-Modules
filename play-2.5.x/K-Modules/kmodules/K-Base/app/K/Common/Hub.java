package K.Common;

import play.Application;
import play.Configuration;
import play.Environment;
import play.cache.CacheApi;
import play.data.FormFactory;
import play.routing.Router;

import javax.inject.Provider;

/**
 * Created by kk on 16/5/6.
 */
public class Hub {

    private static Application application;

    public static void setApplication(Application application) {
        Hub.application = application;
    }

    public static Application application() {
        if (Hub.application == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.application;
    }

    private static Environment environment;

    public static void setEnvironment(Environment environment) {
        Hub.environment = environment;
    }

    public static Environment environment() {
        if (Hub.environment == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.environment;
    }

    private static Configuration configuration;

    public static void setConfiguration(Configuration configuration) {
        Hub.configuration = configuration;
    }

    public static Configuration configuration() {
        if (Hub.configuration == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.configuration;
    }

    private static CacheApi cacheApi;

    public static void setCacheApi(CacheApi cacheApi) {
        Hub.cacheApi = cacheApi;
    }

    public static CacheApi cacheApi() {
        if (Hub.cacheApi == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.cacheApi;
    }

    private static FormFactory formFactory;

    public static void setFormFactory(FormFactory formFactory) {
        Hub.formFactory = formFactory;
    }

    public static FormFactory formFactory() {
        if (Hub.formFactory == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.formFactory;
    }

    private static Provider<Router> routerProvider;

    public static void setRouterProvider(Provider<Router> routerProvider) {
        Hub.routerProvider = routerProvider;
    }

    public static Provider<Router> routerProvider() {
        if (Hub.routerProvider == null) {
            throw new BizLogicException("OnKBaseStartStop 模块没有完成初始化");
        }
        return Hub.routerProvider;
    }
}

