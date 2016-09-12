import filters.ExampleFilter
import play.Environment
import play.Mode
import play.filters.gzip.GzipFilter
import play.http.HttpFilters
import play.mvc.EssentialFilter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class configures filters that run on every request. This
 * class is queried by Play to get a list of filters.

 * Play will automatically use filters from any class called
 * `Filters` that is placed the root package. You can load filters
 * from a different class by adding a `play.http.filters` setting to
 * the `application.conf` configuration file.
 */
@Singleton
class Filters
/**
 * @param env Basic environment settings for the current application.
 * *
 * @param exampleFilter A demonstration filter that adds a header to
 */
@Inject
constructor(private val env: Environment, exampleFilter: ExampleFilter, gzipFilter: GzipFilter) : HttpFilters {
    private val exampleFilter: EssentialFilter
    private val gzipFilter: EssentialFilter

    init {
        this.exampleFilter = exampleFilter
        this.gzipFilter = gzipFilter.asJava()
    }

    override fun filters(): Array<EssentialFilter> {
        // Use the example filter if we're running development mode. If
        // we're running in production or test mode then don't use any
        // filters at all.
        if (env.mode() == Mode.DEV) {
            return arrayOf(exampleFilter, gzipFilter)
        } else {
            return arrayOf(gzipFilter)
        }
    }

}
