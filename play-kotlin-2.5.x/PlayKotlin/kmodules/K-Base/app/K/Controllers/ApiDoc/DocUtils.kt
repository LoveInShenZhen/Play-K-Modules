package K.Controllers.ApiDoc

import K.Aop.annotations.Comment
import K.Aop.annotations.JsonApi
import K.Common.BizLogicException
import K.Common.Helper
import K.Controllers.ApiDoc.Reply.ApiInfo
import K.Controllers.ApiDoc.Reply.FieldInfo
import K.Template.ResourceTemplateHelper
import jodd.bean.BeanUtil
import jodd.util.ReflectUtil

import java.lang.reflect.*
import java.math.BigDecimal
import java.util.*

/**
 * Created by kk on 14/11/4.
 */
object DocUtils {

    private var integer_types: MutableSet<String>? = null
    private var decimal_types: MutableSet<String>? = null
    private var string_types: MutableSet<String>? = null
    private var bool_types: MutableSet<String>? = null
    private var date_type: MutableSet<String>? = null

    init {
        integer_types = HashSet<String>()
        integer_types!!.add(Integer.TYPE.typeName)
        integer_types!!.add(Int::class.java.typeName)
        integer_types!!.add(java.lang.Long.TYPE.typeName)
        integer_types!!.add(Long::class.java.typeName)
        integer_types!!.add(java.lang.Short.TYPE.typeName)
        integer_types!!.add(Short::class.java.typeName)

        decimal_types = HashSet<String>()
        decimal_types!!.add(java.lang.Float.TYPE.typeName)
        decimal_types!!.add(Float::class.java.typeName)
        decimal_types!!.add(java.lang.Double.TYPE.typeName)
        decimal_types!!.add(Double::class.java.typeName)
        decimal_types!!.add(BigDecimal::class.java.typeName)

        string_types = HashSet<String>()
        string_types!!.add(String::class.java.typeName)
        string_types!!.add(Character.TYPE.typeName)
        string_types!!.add(Char::class.java.typeName)

        bool_types = HashSet<String>()
        bool_types!!.add(Boolean TYPE.getTypeName())
        bool_types!!.add(Boolean::class.java.typeName)

        date_type = HashSet<String>()
        date_type!!.add(Date::class.java.typeName)
        date_type!!.add(Calendar::class.java.typeName)

    }

    fun IsBasicType(lookupClass: Class<Any>): Boolean {
        val type_name = lookupClass.typeName
        return integer_types!!.contains(type_name) || decimal_types!!.contains(type_name)
                || string_types!!.contains(type_name) || bool_types!!.contains(type_name)
                || date_type!!.contains(type_name)
    }

    fun IsMapType(lookupClass: Class<Any>): Boolean {
        return ReflectUtil.isTypeOf(lookupClass, Map<Any, Any>::class.java)
    }

    // 判断是否 Array, List, Set 类型
    fun IsListType(lookupClass: Class<Any>): Boolean {
        val isList = ReflectUtil.isTypeOf(lookupClass, List<Any>::class.java)
        val isSet = ReflectUtil.isTypeOf(lookupClass, Set<Any>::class.java)
        val isArray = lookupClass.isArray  //仅仅支持一纬数组
        return isList || isSet || isArray
    }

    fun ApiControllerMethod(controllerCls: Class<Any>, methodName: String): Method? {
        val methods = ReflectUtil.getAccessibleMethods(controllerCls)
        for (method in methods) {
            if (method.name != methodName) {
                continue
            }
            val apiAnno = method.getDeclaredAnnotation(JsonApi::class.java) ?: continue
            return method
        }
        return null
    }

    fun SampleStringField(bean: Any, field: Field) {
        if (String::class.java.typeName != field.genericType.typeName) {
            throw BizLogicException("Field type is not String. It is %s", field.genericType.typeName)
        }
        val comment = field.getAnnotation(Comment::class.java)
        if (comment != null) {
            BeanUtil.declaredForcedSilent.setProperty(bean, field.name, comment.value)
        } else {
            BeanUtil.declaredForcedSilent.setProperty(bean, field.name, "xxxxxx")
        }
    }

    fun FindFieldWithComments(beanClass: Class<Any>, fieldInfoList: MutableList<FieldInfo>) {
        //        Logger.info("==> bean class: {}", beanClass.getName());
        val fields = beanClass.fields
        for (field in fields) {
            if (Modifier.isStatic(field.modifiers)) {
                // 过滤掉静态成员
                //                Logger.info("==> static field: {}", field.getName());
                continue
            }
            val comment = field.getAnnotation(Comment::class.java)
            if (comment != null) {
                val fieldInfo = FieldInfo()
                fieldInfo.field_name = field.name
                fieldInfo.comments = comment.value
                fieldInfo.field_type = field.type.simpleName
                fieldInfo.ownner_class_name = beanClass.name

                fieldInfoList.add(fieldInfo)
            }
        }
        for (field in fields) {
            if (Modifier.isStatic(field.modifiers)) {
                // 过滤掉静态成员
                //                Logger.info("==> static field: {}", field.getName());
                continue
            }

            // 如果是基础类型, 就不需要再进行处理了
            if (IsBasicType(field.type)) {
                continue
            }

            if (field.type.isArray) {
                // 如果 filed 是数组类型
                val element_class = field.type.componentType
                FindFieldWithComments(element_class, fieldInfoList)
                continue
            }

            val field_type = field.genericType
            if (field_type is ParameterizedType) {
                // field 是泛型容器
                if (ReflectUtil.isTypeOf(field.type, List<Any>::class.java) || ReflectUtil.isTypeOf(field.type, Set<Any>::class.java)) {
                    // List<?> or Set<?>
                    val gpTypeArgs = field_type.actualTypeArguments
                    val element_class = Helper.LoadClass(gpTypeArgs[0].typeName)
                    FindFieldWithComments(element_class, fieldInfoList)
                }
                if (ReflectUtil.isTypeOf(field.type, Map<Any, Any>::class.java)) {
                    val gpTypeArgs = field_type.actualTypeArguments
                    val element_class = Helper.LoadClass(gpTypeArgs[1].typeName)
                    FindFieldWithComments(element_class, fieldInfoList)
                }
                continue
            }

            // 普通 java bean class
            FindFieldWithComments(field.type, fieldInfoList)
        }

    }

    @Throws(IllegalAccessException::class, NoSuchMethodException::class, InvocationTargetException::class)
    fun SetupSampleDataForBasicTypeFields(bean: Any) {
        val bean_class = bean.javaClass
        val fields = ReflectUtil.getAccessibleFields(bean_class)

        for (field in fields) {
            if (Modifier.isStatic(field.modifiers)) {
                // 过滤掉静态成员
                continue
            }

            if (IsBasicType(field.type)) {

                if (field.name.endsWith("_amt") || field.name.endsWith("_amount")) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 100)
                    continue
                }

                if (field.name == "page_num") {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 1)
                    continue
                }

                if (field.name == "page_size") {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 10)
                    continue
                }

                if (field.name == "total_pages") {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 1)
                    continue
                }

                if (field.name == "page_num") {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 1)
                    continue
                }

                if (field.name == "page_num") {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, 1)
                    continue
                }

                if (BeanUtil.declaredForcedSilent.getProperty<Any>(bean, field.name) != null) {
                    continue
                }

                if (field.type.typeName == String::class.java.typeName) {
                    val comment = field.getAnnotation(Comment::class.java)
                    if (comment != null) {
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.name, comment.value)
                    } else {
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.name, "XXXXXX")
                    }
                    continue
                }

                if (field.type.typeName == Date::class.java.typeName) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, Date())
                    continue
                }

                if (field.type.typeName == Calendar::class.java.typeName) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, Date())
                    continue
                }

                if (field.type.typeName == Boolean.TYPE.getTypeName() || field.type.typeName == Boolean::class.java.typeName) {
                    BeanUtil.declaredForcedSilent.setProperty(bean, field.name, true)
                    continue
                }
            } else {
                // 非基础类型, 在此, 需要再次进行检查, 是泛型容器, 还是普通对象
                if (field.type.isArray) {
                    // 如果 filed 是数组类型
                    // 不处理
                }

                val field_type = field.genericType
                if (field_type is ParameterizedType) {

                    var field_obj: Any? = BeanUtil.declaredForcedSilent.getProperty<Any>(bean, field.name)

                    // field 是泛型容器
                    if (ReflectUtil.isTypeOf(field.type, List<Any>::class.java) || ReflectUtil.isTypeOf(field.type, Set<Any>::class.java)) {
                        if (field_obj == null) {
                            // 不处理
                            continue
                        }
                        // List<>  or Set<>
                        val gpTypeArgs = field_type.actualTypeArguments
                        val element_class = Helper.LoadClass(gpTypeArgs[0].typeName)

                        val size = (ReflectUtil.invokeDeclared(field_obj, "size") as Int).toInt()
                        if (size == 0) {
                            val paramClasses = arrayOf(Any::class.java)
                            var params: Array<Any>

                            val element_1 = NewInstance(element_class)
                            if (element_1 != null) {
                                params = arrayOf(element_1)
                                ReflectUtil.invoke(field_obj, "add", paramClasses, params)
                                SetupSampleDataForBasicTypeFields(element_1)
                            }

                            val element_2 = NewInstance(element_class)
                            if (element_2 != null) {
                                params = arrayOf(element_2)
                                ReflectUtil.invokeDeclared(field_obj, "add", paramClasses, params)
                                SetupSampleDataForBasicTypeFields(element_2)
                            }

                        }

                        continue
                    }

                    if (ReflectUtil.isTypeOf(field.type, Map<Any, Any>::class.java)) {
                        // 不处理
                        continue
                    }

                    if (field_obj == null) {
                        field_obj = NewInstance(field.type)
                        BeanUtil.declaredForcedSilent.setProperty(bean, field.name, field_obj)
                        SetupSampleDataForBasicTypeFields(field_obj)
                        continue
                    } else {
                        SetupSampleDataForBasicTypeFields(field_obj)
                        continue
                    }

                } else {
                    // 其他非基础类型对象 (非泛型)
                    var field_obj: Any? = BeanUtil.declaredForcedSilent.getProperty<Any>(bean, field.name)
                    if (field_obj == null) {
                        field_obj = NewInstance(field.type)
                        if (field_obj != null) {
                            BeanUtil.declaredForcedSilent.setProperty(bean, field.name, field_obj)
                            SetupSampleDataForBasicTypeFields(field_obj)
                        } else {
                            // 不处理
                            //                            Logger.error("==> field_name:{} type: {}",
                            //                                    field.getName(),
                            //                                    field.getType().getName());
                        }
                        continue
                    } else {
                        SetupSampleDataForBasicTypeFields(field_obj)
                        continue
                    }
                }

            }
        }
    }

    private fun NewInstance(cls: Class<Any>): Any? {
        try {
            return cls.newInstance()
        } catch (ex: Exception) {
            return null
        }

    }

    fun GeneratApiMarkdown(definedAPIs: DefinedAPIs): String {
        try {
            val md = ResourceTemplateHelper.Process(DefinedAPIs::class.java,
                    "ApiDocTemplates/ApiDoc",
                    definedAPIs.apiDefinition)
            return md
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

    fun GeneratApiSample(api_url: String, definedAPIs: DefinedAPIs): String {
        try {
            val apiInfo = definedAPIs.getApiInfoByRoute(api_url) ?: return "<p>Can not find api by url</p>"
            val html = ResourceTemplateHelper.Process(DefinedAPIs::class.java,
                    "ApiDocTemplates/ApiSample.html",
                    apiInfo)
            return html
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }

    fun GeneratApiTestPage(definedAPIs: DefinedAPIs): String {
        try {

            val html = ResourceTemplateHelper.Process(DefinedAPIs::class.java,
                    "ApiDocTemplates/ApiTest.html",
                    definedAPIs.apiDefinition)
            return html
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }
}
