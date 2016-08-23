package K.Service;

import java.util.ArrayList;

/**
 * Created by kk on 14-1-2.
 */
public class ServiceMgr {

    public static ServiceMgr Instance;

    static {
        Instance = new ServiceMgr();
    }

    private ArrayList<IService> services;

    public ServiceMgr() {
        services = new ArrayList<IService>();
    }

    public void RegService(IService service) {
        services.add(service);
    }

    public boolean Exists(String service_name) {
        for (IService service : services) {
            if (service.Name().equals(service_name)) {
                return true;
            }
        }
        return false;
    }

    public IService getService(String service_name) {
        for (IService service : services) {
            if (service.Name().equals(service_name)) {
                return service;
            }
        }
        return null;
    }

    public void StartAll() {
        for (int i = 0; i < services.size(); ++i) {
            services.get(i).Start();
        }
    }

    public void StopAll() {
        for (int i = services.size() - 1; i >= 0; --i) {
            services.get(i).Stop();
        }
    }
}
