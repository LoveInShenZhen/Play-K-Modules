package services

/**
 * This interface demonstrates how to create a component that is injected
 * into a controller. The interface represents a counter that returns a
 * incremented number each time it is called.

 * The [Modules] class binds this interface to the
 * [AtomicCounter] implementation.
 */
interface Counter {
    fun nextCount(): Int
}
