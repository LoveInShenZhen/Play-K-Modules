import K.Init.InitKBase;
import play.Application;
import play.GlobalSettings;

/**
 * Created by kk on 15/12/2.
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        super.onStart(app);
        InitKBase.OnStart();
    }

    @Override
    public void onStop(Application app) {
        super.onStop(app);
        InitKBase.OnStop();
    }
}
