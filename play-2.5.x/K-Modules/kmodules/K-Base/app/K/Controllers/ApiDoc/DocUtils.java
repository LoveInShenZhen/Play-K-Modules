package K.Controllers.ApiDoc;

import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.BizLogicException;
import K.Common.Helper;
import K.Controllers.ApiDoc.Reply.ApiInfo;
import K.Controllers.ApiDoc.Reply.FieldInfo;
import K.Template.ResourceTemplateHelper;
import jodd.bean.BeanUtil;
import jodd.datetime.JDateTime;
import jodd.util.ReflectUtil;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by kk on 14/11/4.
 */
public class DocUtils {

    private static Set<String> integer_types;
    private static Set<String> decimal_types;
    private static Set<String> string_types;
    private static Set<String> bool_types;
    private static Set<String> date_type;

    static {
        integer_types = new HashSet<>();
        integer_types.add(int.class.getTypeName());
        integer_types.add(Integer.class.getTypeName());
        integer_types.add(long.class.getTypeName());
        integer_types.add(Long.class.getTypeName());
        integer_types.add(short.class.getTypeName());
        integer_types.add(Short.class.getTypeName());

        decimal_types = new HashSet<>();
        decimal_types.add(float.class.getTypeName());
        decimal_types.add(Float.class.getTypeName());
        decimal_types.add(double.class.getTypeName());
        decimal_types.add(Double.class.getTypeName());
        decimal_types.add(BigDecimal.class.getTypeName());

        string_types = new HashSet<>();
        string_types.add(String.class.getTypeName());
        string_types.add(char.class.getTypeName());
        string_types.add(Character.class.getTypeName());

        bool_types = new HashSet<>();
        bool_types.add(boolean.class.getTypeName());
        bool_types.add(Boolean.class.getTypeName());

        date_type = new HashSet<>();
        date_type.add(Date.class.getTypeName());
        date_type.add(Calendar.class.getTypeName());

    }

    public static boolean IsBasicType(Class lookupClass) {
        String type_name = lookupClass.getTypeName();
        return integer_types.contains(type_name) || decimal_types.contains(type_name)
                || string_types.contains(type_name) || bool_types.contains(type_name)
                || date_type.contains(type_name);
    }

    public static boolean IsMapType(Class lookupClass) {
        return ReflectUtil.isTypeOf(lookupClass, Map.class);
    }

    // 判断是否 Array, List, Set 类型
    public static boolean IsListType(Class lookupClass) {
        boolean isList = ReflectUtil.isTypeOf(lookupClass, List.class);
        boolean isSet = ReflectUtil.isTypeOf(lookupClass, Set.class);
        boolean isArray = lookupClass.isArray();  //仅仅支持一纬数组
        return isList || isSet || isArray;
    }

    public static Method ApiControllerMethod(Class controllerCls, String methodName) {
        Method[] methods = ReflectUtil.getAccessibleMethods(controllerCls);
        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            JsonApi apiAnno = method.getDeclaredAnnotation(JsonApi.class);
            if (apiAnno == null) {
                continue;
            }
            return method;
        }
        return null;
    }

    public static void SampleStringField(Object bean, Field field) {
        if (!String.class.getTypeName().equals(field.getGenericType().getTypeName())) {
            throw new BizLogicException("Field type is not String. It is %s", field.getGenericType().getTypeName());
        }
        Comment comment = field.getAnnotation(Comment.class);
        if (comment != null) {
            BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), comment.value());
        } else {
            BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), "xxxxxx");
        }
    }

    public static void FindFieldWithComments(Class beanClass, List<FieldInfo> fieldInfoList) {
//        Logger.info("==> bean class: {}", beanClass.getName());
        Field[] fields = beanClass.getFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                // 过滤掉静态成员
//                Logger.info("==> static field: {}", field.getName());
                continue;
            }
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.field_name = field.getName();
                fieldInfo.comments = comment.value();
                fieldInfo.field_type = field.getType().getSimpleName();
                fieldInfo.ownner_class_name = beanClass.getName();

                fieldInfoList.add(fieldInfo);
            }
        }
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                // 过滤掉静态成员
//                Logger.info("==> static field: {}", field.getName());
                continue;
            }

            // 如果是基础类型, 就不需要再进行处理了
            if (IsBasicType(field.getType())) {
                continue;
            }

            if (field.getType().isArray()) {
                // 如果 filed 是数组类型
                Class element_class = field.getType().getComponentType();
                FindFieldWithComments(element_class, fieldInfoList);
                continue;
            }

            Type field_type = field.getGenericType();
            if (field_type instanceof ParameterizedType) {
                // field 是泛型容器
                if (ReflectUtil.isTypeOf(field.getType(), List.class) ||
                        ReflectUtil.isTypeOf(field.getType(), Set.class)) {
                    // List<?> or Set<?>
                    Type[] gpTypeArgs = ((ParameterizedType) field_type).getActualTypeArguments();
                    Class element_class = Helper.LoadClass(gpTypeArgs[0].getTypeName());
                    FindFieldWithComments(element_class, fieldInfoList);
                }
                if (ReflectUtil.isTypeOf(field.getType(), Map.class)) {
                    Type[] gpTypeArgs = ((ParameterizedType) field_type).getActualTypeArguments();
                    Class element_class = Helper.LoadClass(gpTypeArgs[1].getTypeName());
                    FindFieldWithComments(element_class, fieldInfoList);
                }
                continue;
            }

            // 普通 java bean class
            FindFieldWithComments(field.getType(), fieldInfoList);
        }

    }

    public static void SetupSampleDataForBasicTypeFields(Object bean) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class bean_class = bean.getClass();
        Field[] fields = ReflectUtil.getAccessibleFields(bean_class);

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                // 过滤掉静态成员
                continue;
            }

            if (IsBasicType(field.getType())) {

                if (field.getName().endsWith("_amt") || field.getName().endsWith("_amount")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 100);
                    continue;
                }

                if (field.getName().equals("page_num")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 1);
                    continue;
                }

                if (field.getName().equals("page_size")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 10);
                    continue;
                }

                if (field.getName().equals("total_pages")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 1);
                    continue;
                }

                if (field.getName().equals("page_num")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 1);
                    continue;
                }

                if (field.getName().equals("page_num")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), 1);
                    continue;
                }

                if (BeanUtil.declaredForcedSilent.getProperty(bean, field.getName()) != null) {
                    continue;
                }

                if (field.getType().getTypeName().equals(String.class.getTypeName())) {
                    Comment comment = field.getAnnotation(Comment.class);
                    if (comment != null) {
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), comment.value());
                    } else {
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), "XXXXXX");
                    }
                    continue;
                }

                if (field.getType().getTypeName().equals(Date.class.getTypeName())) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), new Date());
                    continue;
                }

                if (field.getType().getTypeName().equals(Calendar.class.getTypeName())) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), Calendar.getInstance());
                    continue;
                }

                if (field.getType().getTypeName().equals(JDateTime.class.getTypeName())) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), new JDateTime());
                    continue;
                }

                if (field.getType().getTypeName().equals(boolean.class.getTypeName()) ||
                        field.getType().getTypeName().equals(Boolean.class.getTypeName())) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), true);
                    continue;
                }
            } else {
                // 非基础类型, 在此, 需要再次进行检查, 是泛型容器, 还是普通对象
                if (field.getType().isArray()) {
                    // 如果 filed 是数组类型
                    // 不处理
                }

                Type field_type = field.getGenericType();
                if (field_type instanceof ParameterizedType) {

                    Object field_obj = BeanUtil.declaredForcedSilent.getProperty(bean, field.getName());

                    // field 是泛型容器
                    if (ReflectUtil.isTypeOf(field.getType(), List.class) ||
                            ReflectUtil.isTypeOf(field.getType(), Set.class)) {
                        if (field_obj == null) {
                            // 不处理
                            continue;
                        }
                        // List<>  or Set<>
                        Type[] gpTypeArgs = ((ParameterizedType) field_type).getActualTypeArguments();
                        Class element_class = Helper.LoadClass(gpTypeArgs[0].getTypeName());

                        int size = ((Integer) ReflectUtil.invokeDeclared(field_obj, "size")).intValue();
                        if (size == 0) {
                            Class[] paramClasses = new Class[]{Object.class};
                            Object[] params;

                            Object element_1 = NewInstance(element_class);
                            if (element_1 != null) {
                                params = new Object[]{element_1};
                                ReflectUtil.invoke(field_obj, "add", paramClasses, params);
                                SetupSampleDataForBasicTypeFields(element_1);
                            }

                            Object element_2 = NewInstance(element_class);
                            if (element_2 != null) {
                                params = new Object[]{element_2};
                                ReflectUtil.invokeDeclared(field_obj, "add", paramClasses, params);
                                SetupSampleDataForBasicTypeFields(element_2);
                            }

                        }

                        continue;
                    }

                    if (ReflectUtil.isTypeOf(field.getType(), Map.class)) {
                        // 不处理
                        continue;
                    }

                    if (field_obj == null) {
                        field_obj = NewInstance(field.getType());
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), field_obj);
                        SetupSampleDataForBasicTypeFields(field_obj);
                        continue;
                    } else {
                        SetupSampleDataForBasicTypeFields(field_obj);
                        continue;
                    }

                } else {
                    // 其他非基础类型对象 (非泛型)
                    Object field_obj = BeanUtil.declaredForcedSilent.getProperty(bean, field.getName());
                    if (field_obj == null) {
                        field_obj = NewInstance(field.getType());
                        if (field_obj != null) {
                            BeanUtil.declaredForcedSilent.setProperty(bean, field.getName(), field_obj);
                            SetupSampleDataForBasicTypeFields(field_obj);
                        } else {
                            // 不处理
//                            Logger.error("==> field_name:{} type: {}",
//                                    field.getName(),
//                                    field.getType().getName());
                        }
                        continue;
                    } else {
                        SetupSampleDataForBasicTypeFields(field_obj);
                        continue;
                    }
                }

            }
        }
    }

    private static Object NewInstance(Class cls) {
        try {
            return cls.newInstance();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String  GeneratApiMarkdown(DefinedAPIs definedAPIs) {
        try {
            String md = ResourceTemplateHelper.Process(DefinedAPIs.class,
                    "ApiDocTemplates/ApiDoc",
                    definedAPIs.getApiDefinition());
            return md;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static String GeneratApiSample(String api_url, DefinedAPIs definedAPIs) {
        try {
            ApiInfo apiInfo = definedAPIs.getApiInfoByRoute(api_url);
            if (apiInfo == null) {
                return "<p>Can not find api by url</p>";
            }
            String html = ResourceTemplateHelper.Process(DefinedAPIs.class,
                    "ApiDocTemplates/ApiSample.html",
                    apiInfo);
            return html;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String GeneratApiTestPage(DefinedAPIs definedAPIs) {
        try {

            String html = ResourceTemplateHelper.Process(DefinedAPIs.class,
                    "ApiDocTemplates/ApiTest.html",
                    definedAPIs.getApiDefinition());
            return html;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
