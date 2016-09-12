package controllers

import k.common.Helper
import k.ebean.DB
import play.mvc.Controller
import play.mvc.Result
import play.mvc.Results

/**
 * Created by kk on 16/8/25.
 */
class Samples : Controller() {

    fun test() : Result {

        DB.RunInTransaction({
            Helper.DLog("kk")
        })
        return Results.ok("todo")
    }


}