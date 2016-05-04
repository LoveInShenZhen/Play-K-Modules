package models.K.EbeanConfig;


import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;
import models.K.BgTask.PlanTask;
import play.Logger;

import java.util.Set;

/**
 * Created by kk on 15/1/20.
 */
public class NewTaskPersistController implements BeanPersistController {

    public NewTaskPersistController() {
        Logger.debug("Ebean server load NewTaskPersistController");
    }

    @Override
    public int getExecutionOrder() {
        return 11;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        String bean_cls_name = cls.getName();
        if (bean_cls_name.equals(PlanTask.class.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preInsert(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public boolean preDelete(BeanPersistRequest<?> request) {
        return true;
    }

    @Override
    public void postInsert(BeanPersistRequest<?> request) {
        String bean_cls_name = request.getBean().getClass().getName();
//        Logger.debug("New Task: {}", Helper.ToJsonStringPretty(request.getBean()));
        if (bean_cls_name.equals(PlanTask.class.getName())) {
            request.getTransaction().putUserObject(PlanTask.class.getName(), "");
        }
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        // do nothing
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
        // do nothing
    }

    @Override
    public void postLoad(Object bean, Set<String> includedProperties) {
        // do nothing
    }
}
