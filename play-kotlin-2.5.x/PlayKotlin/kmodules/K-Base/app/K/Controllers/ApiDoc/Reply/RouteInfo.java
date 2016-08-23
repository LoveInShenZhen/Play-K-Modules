package K.Controllers.ApiDoc.Reply;

import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import jodd.util.ReflectUtil;

import java.lang.reflect.Method;

/**
 * Created by kk on 15/7/20.
 */
public class RouteInfo {

    @Comment("API http method: GET or POST")
    private String http_method;

    @Comment("API url")
    private String url;

    @Comment("控制器方法全路径")
    private String controller_path;

    public RouteInfo(String http_method, String url, String controller_path) {
        this.http_method = http_method;
        this.url = url;
        this.controller_path = controller_path;
    }

    public String ApiUrl() {
        return url;
    }

    public Class ControllerClass() throws ClassNotFoundException {
        return RouteInfo.class.getClassLoader().loadClass(this.ControllerClassName());
    }

    public String ControllerClassName() {
        int l = controller_path.lastIndexOf('.');
        return controller_path.substring(0, l);
    }

    public String ControllerMethodName() {
        int l = controller_path.lastIndexOf('.');
        int r = controller_path.indexOf('(');
        return controller_path.substring(l + 1, r);
    }

    public Method GetControllerMethod() throws ClassNotFoundException {
        Method[] methods = ReflectUtil.getAccessibleMethods(ControllerClass());
        for (Method method : methods) {
            if (method.getName().equals(ControllerMethodName())) {
                return method;
            }
        }

        return null;
    }

    public JsonApi GetJsonApiAnnotation() throws ClassNotFoundException {
        Method method = GetControllerMethod();
        if (method == null) {
            return null;
        }
        JsonApi apiAnno = method.getAnnotation(JsonApi.class);
        return apiAnno;
    }

    public String ControllerComment() throws ClassNotFoundException {
        Class controller_cls = ControllerClass();
        Comment comment_anno = (Comment) controller_cls.getAnnotation(Comment.class);
        if (comment_anno != null) {
            return comment_anno.value();
        } else {
            return ControllerClassName();
        }
    }


}
