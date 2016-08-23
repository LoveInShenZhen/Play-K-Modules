package K.AppModules;

import K.AppModules.Component.OnKBaseStartStop;
import K.Controllers.ApiDoc.DefinedAPIs;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * Created by kk on 16/5/5.
 */
public class KBaseModule extends Module {
    @Override
    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {

        return seq(
                bind(OnKBaseStartStop.class).toSelf().eagerly()
        );
    }
}