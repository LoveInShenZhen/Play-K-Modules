package K.Controllers.PlanTask;

import K.Controllers.JsonpController;
import K.Service.PlanTasks.PlanTaskService;
import K.Service.ServiceMgr;
import play.mvc.Result;

/**
 * Created by kk on 15/12/2.
 */
public class PlanTaskNotify extends JsonpController{

    public Result NewPlanTask() {
        PlanTaskService planTaskService = (PlanTaskService) ServiceMgr.Instance.getService(PlanTaskService.ServiceName);
        if (planTaskService != null) {
            planTaskService.NotifyNewTask();
        }
        return ok("OK");
    }

}
