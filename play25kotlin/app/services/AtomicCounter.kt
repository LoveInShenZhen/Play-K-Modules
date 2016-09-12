package services

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.*

/**
 * This class is a concrete implementation of the [Counter] trait.
 * It is configured for Guice dependency injection in the [Module]
 * class.

 * This class has a [Singleton] annotation because we need to make
 * sure we only use one counter per application. Without this
 * annotation we would get a new instance every time a [Counter] is
 * injected.
 */
@Singleton
class AtomicCounter : Counter {

    private val atomicCounter = AtomicInteger()

    override fun nextCount(): Int {
        return atomicCounter.andIncrement
    }

}
