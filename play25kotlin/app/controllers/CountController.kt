package controllers

import javax.inject.*
import play.*
import play.mvc.*

import services.Counter

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class contains an
 * action that shows an incrementing count to users. The [Counter]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class CountController
@Inject
constructor(private val counter: Counter) : Controller() {

    /**
     * An action that responds with the [Counter]'s current
     * count. The result is plain text. This action is mapped to
     * `GET` requests with a path of `/count`
     * requests by an entry in the `routes` config file.
     */
    fun count(): Result {
        return Results.ok(Integer.toString(counter.nextCount()))
    }

}
