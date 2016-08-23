package models.K.EbeanConfig;

import K.Aop.annotations.WithPersistLog;
import K.Common.Helper;
import com.avaje.ebean.event.BeanPersistController;
import com.avaje.ebean.event.BeanPersistRequest;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import play.Logger;
import play.libs.Json;

import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kk on 14/11/28.
 */
public class MyBeanPersistController implements BeanPersistController {

    private Set<String> registed_bean_class_names;

    public MyBeanPersistController() {
        Logger.debug("Ebean server load MyBeanPersistController");
        registed_bean_class_names = new HashSet<>();
        try {
            ClassPath cp = ClassPath.from(MyBeanPersistController.class.getClassLoader());
            String ModelsPackage = "models";
            ImmutableSet<ClassPath.ClassInfo> classes = cp.getTopLevelClassesRecursive(ModelsPackage);
            for (ClassPath.ClassInfo c : classes) {
                Class<?> BeanClass = c.load();
                Entity entityAnno = BeanClass.getAnnotation(Entity.class);
                if (entityAnno == null) {
                    continue;
                }
                WithPersistLog withPersistLogAnno = BeanClass.getAnnotation(WithPersistLog.class);
                if (withPersistLogAnno == null) {
                    continue;
                }
                registed_bean_class_names.add(BeanClass.getName());
//                Logger.debug("MyBeanPersistController regist bean class: {}", BeanClass.getName());
            }
        } catch (Exception ex) {
            Logger.error(ex.getMessage());
        }
    }

    // When there are multiple BeanPersistController's for a given entity type this controls the order in which they are executed.
    // Lowest values are executed first.
    @Override
    public int getExecutionOrder() {
        return 10;
    }

    @Override
    public boolean isRegisterFor(Class<?> cls) {
        return registed_bean_class_names.contains(cls.getName());
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
        WriteLog(request, request.getBean(), "Insert");
    }

    @Override
    public void postUpdate(BeanPersistRequest<?> request) {
        WriteLog(request, request.getBean(), "Update");
    }

    @Override
    public void postDelete(BeanPersistRequest<?> request) {
        WriteLog(request, request.getBean(), "Delete");
    }

    private void WriteLog(BeanPersistRequest<?> request, Object bean, String action_type) {
        PersistLog plog = new PersistLog();
        plog.bean_class = bean.getClass().getName();
        plog.action_type = action_type;
        plog.bean_data = Json.toJson(bean);
        StringBuilder sb = getBuffer(request);
        sb.append(Helper.ToJsonStringPretty(plog)).append("\n");

    }

    private StringBuilder getBuffer(BeanPersistRequest<?> request) {
        Object obj = request.getTransaction().getUserObject(MyBeanPersistController.class.getName());
        if (obj == null) {
            StringBuilder sb = new StringBuilder();
            request.getTransaction().putUserObject(MyBeanPersistController.class.getName(), sb);
            return sb;
        } else {
            return (StringBuilder) obj;
        }
    }
}
