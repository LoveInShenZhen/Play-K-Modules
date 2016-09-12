package controllers

import akka.actor.ActorSystem
import jodd.datetime.JDateTime
import play.mvc.Controller
import play.mvc.Result
import play.mvc.Results
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.function.Function
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This controller contains an action that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.

 * @param actorSystem We need the [ActorSystem]'s
 * * [Scheduler] to run code after a delay.
 * *
 * @param exec We need a Java [Executor] to apply the result
 * * of the [CompletableFuture] and a Scala
 * * [ExecutionContext] so we can use the Akka [Scheduler].
 * * An [ExecutionContextExecutor] implements both interfaces.
 */
@Singleton
class AsyncController
@Inject
constructor(private val actorSystem: ActorSystem, private val exec: ExecutionContextExecutor) : Controller() {

    /**
     * An action that returns a plain text message after a delay
     * of 1 second.

     * The configuration in the `routes` file means that this method
     * will be called when the application receives a `GET` request with
     * a path of `/message`.
     */
    fun message(): CompletionStage<Result> {
        return getFutureMessage(3, TimeUnit.SECONDS).thenApplyAsync<Result>(Function<String, Result> { Results.ok(it) }, exec)
    }

    private fun getFutureMessage(time: Long, timeUnit: TimeUnit): CompletionStage<String> {
        val future = CompletableFuture<String>()
        actorSystem.scheduler().scheduleOnce(
                Duration.create(time, timeUnit),
                Runnable {
                    val now = JDateTime()
                    future.complete("Hi! ${now.toString("YYYY-MM-DD hh:mm:ss")}\n") },
                exec)
        return future
    }

}
