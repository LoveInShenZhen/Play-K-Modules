package K.Controllers.ApiDoc

import java.util
import K.Controllers.ApiDoc.Reply.RouteInfo
import play.api.Play
import play.api.routing.Router

import scala.collection.JavaConversions._

/**
 * Created by kk on 15/7/20.
 */
object RouteHelper {

  def GetAllRoutes() : util.List[RouteInfo] = {
    Play.current.injector.instanceOf[Router].documentation.map(ro => {
      new RouteInfo(ro._1, ro._2, ro._3)
    })

  }

}
