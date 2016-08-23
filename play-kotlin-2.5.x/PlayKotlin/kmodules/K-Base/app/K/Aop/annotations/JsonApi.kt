package K.Aop.annotations

import K.Controllers.ApiDoc.SampleForm.PostFormSample
import play.mvc.With
import kotlin.reflect.KClass

/**
 * Created by kk on 14-6-10.
 */
@With(K.Aop.actions.JsonApiAction::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonApi(val Transactional: Boolean = true, val UseEtag: Boolean = false, val ReplyClass : KClass<*>, val ApiMethodType:

String = "GET", val PostDataClass : KClass<*> = PostFormSample::class)
