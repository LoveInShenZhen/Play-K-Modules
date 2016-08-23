package K.Controllers.PlanTask

import K.Controllers.JsonpController
import K.Service.PlanTasks.PlanTaskService
import K.Service.ServiceMgr
import play.mvc.Result
import play.mvc.Results

/**
 * Created by kk on 15/12/2.
 */
class PlanTaskNotify : JsonpController() {

    fun NewPlanTask(): Result {
        val planTaskService = ServiceMgr.Instance.getService(PlanTaskService.ServiceName) as PlanTaskService
        planTaskService?.NotifyNewTask()
        return Results.ok("OK")
    }

}
