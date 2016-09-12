package services

import java.time.Clock
import java.time.Instant
import java.util.concurrent.CompletableFuture
import javax.inject.*
import play.Logger
import play.inject.ApplicationLifecycle

/**
 * This class demonstrates how to run code when the
 * application starts and stops. It starts a timer when the
 * application starts. When the application stops it prints out how
 * long the application was running for.

 * This class is registered for Guice dependency injection in the
 * [Module] class. We want the class to start when the application
 * starts, so it is registered as an "eager singleton". See the code
 * in the [Module] class to see how this happens.

 * This class needs to run code when the server stops. It uses the
 * application's [ApplicationLifecycle] to register a stop hook.
 */
@Singleton
class ApplicationTimer
@Inject
constructor(private val clock: Clock, private val appLifecycle: ApplicationLifecycle) {
    private val start: Instant

    init {
        // This code is called when the application starts.
        start = clock.instant()
        Logger.info("ApplicationTimer demo: Starting application at " + start)

        // When the application starts, register a stop hook with the
        // ApplicationLifecycle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook {
            val stop = clock.instant()
            val runningTime = stop.epochSecond - start.epochSecond
            Logger.info("ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s.")
            CompletableFuture.completedFuture<Any>(null)
        }
    }

}
