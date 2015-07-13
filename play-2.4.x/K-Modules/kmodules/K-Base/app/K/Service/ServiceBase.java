package K.Service;

/**
 * Created by kk on 13-12-26.
 */
public abstract class ServiceBase implements IService {
    private String name_;
    private boolean running_;

    public ServiceBase(String Name) {
        name_ = Name;
        running_ = false;
    }

    public String Name() {
        return name_;
    }

    public boolean Running() {
        return running_;
    }

    public void setRunning(boolean running) {
        running_ = running;
    }

    public void Start() {
        throw new RuntimeException(String.format("Please implement Start() method in %s", this.getClass().getName()));
    }

    public boolean Stop() {
        throw new RuntimeException(String.format("Please implement Stop() method in %s", this.getClass().getName()));
    }
}
