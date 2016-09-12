package filters

import akka.stream.Materializer
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.function.Function
import javax.inject.*
import play.mvc.*
import play.mvc.Http.RequestHeader


/**
 * This is a simple filter that adds a header to all requests. It's
 * added to the application's list of filters by the
 * [ExampleFilters] class.
 */
@Singleton
class ExampleFilter
/**
 * @param mat This object is needed to handle streaming of requests
 * * and responses.
 * *
 * @param exec This class is needed to execute code asynchronously.
 * * It is used below by the `thenAsyncApply` method.
 */
@Inject
constructor(mat: Materializer, private val exec: Executor) : Filter(mat) {

    override fun apply(
            next: Function<RequestHeader, CompletionStage<Result>>,
            requestHeader: RequestHeader): CompletionStage<Result> {

        return next.apply(requestHeader).thenApplyAsync<Result>(
                Function { result:Result -> result.withHeader("X-ExampleFilter", "foo") },
                exec)

    }

}
