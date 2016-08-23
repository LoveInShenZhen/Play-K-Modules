package K.Controllers.Sample

import models.SampleModel
import play.mvc.Controller
import play.mvc.Result

/**
 * Created by kk on 16/6/27.
 */
class SampleTest : Controller() {

    fun Test(): Result {

        return ok("Kotlin Controller Run OK !!!")
    }


}