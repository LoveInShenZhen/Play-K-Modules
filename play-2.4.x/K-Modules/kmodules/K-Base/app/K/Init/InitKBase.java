package K.Init;

import play.Configuration;

/**
 * Created by kk on 15/7/13.
 */
public interface InitKBase {

    void OnStart();

    void OnStop();

    Configuration Config();
}
