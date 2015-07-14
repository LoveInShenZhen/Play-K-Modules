package K.Init;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import play.Configuration;
import play.Environment;


/**
 * Created by kk on 15/7/13.
 */

@Singleton
public class KBaseModule extends AbstractModule {

    Environment environment;

    Configuration configuration;

    public KBaseModule(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(InitKBase.class)
                .to(InitKBaseImp.class)
                .asEagerSingleton();
    }

//    @Override
//    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
//        return seq(bind(InitKBase.class)
//                        .to(InitKBaseImp.class)
//                        .eagerly(),
//                bind(EventBusService.class)
//                .to(EventServiceImp.class)
//        );
//    }
}
