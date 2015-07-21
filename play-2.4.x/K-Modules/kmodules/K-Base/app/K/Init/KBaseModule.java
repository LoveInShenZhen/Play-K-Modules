package K.Init;

import com.google.inject.Singleton;
import play.Configuration;
import play.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;


/**
 * Created by kk on 15/7/13.
 */

@Singleton
public class KBaseModule extends Module {

    Environment environment;

    Configuration configuration;

    public KBaseModule(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    public Seq<Binding<?>> bindings(play.api.Environment environment, play.api.Configuration configuration) {

        return seq(bind(InitKBase.class).to(InitKBaseImp.class).eagerly());
    }

//    @Override
//    protected void configure() {
//        bind(InitKBase.class)
//                .to(InitKBaseImp.class)
//                .asEagerSingleton();
//    }

}
