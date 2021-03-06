package com.lightningkite.kotlinx.observable.property


/**
 * An observable property where you can push and pop states.
 * Notifies listeners of the current state.
 * Created by joseph on 1/19/18.
 */
class StackObservableProperty<T>() : MutableObservableProperty<T> {
    private val internalStack = ArrayList<T>()
    private val listeners = ArrayList<(T) -> Unit>()
    val stack: List<T> get() = internalStack

    /**
     * Creates a [StackObservableProperty] with a starting state.
     */
    constructor(firstItem: T) : this() {
        internalStack.add(firstItem)
    }

    /**
     * Creates a [StackObservableProperty] with a starting state.
     */
    constructor(firstItemGenerator: (StackObservableProperty<T>) -> T) : this() {
        internalStack.add(firstItemGenerator(this))
    }

    override var value: T
        get() {
            if (stack.isEmpty()) throw NoStatesLeftException()
            return internalStack.last()
        }
        set(value) {
            if (internalStack.isEmpty()) internalStack.add(value)
            else internalStack[internalStack.lastIndex] = value
            listeners.forEach { it.invoke(value) }
        }

    override fun add(element: (T) -> Unit): Boolean = listeners.add(element)
    override fun remove(element: (T) -> Unit): Boolean = listeners.remove(element)

    /**
     * Adds a state to the bottom of the stack silently.
     */
    fun prepend(element: T) {
        internalStack.add(0, element)
    }

    /**
     * Pushes a new state onto the stack and notifies the listeners.
     */
    fun push(element: T) {
        internalStack.add(element)
        listeners.forEach { it.invoke(element) }
    }

    /**
     * Swaps the top state in the stack for another and notifies the listeners.
     */
    fun swap(element: T) {
        internalStack[internalStack.lastIndex] = element
        listeners.forEach { it.invoke(element) }
    }

    /**
     * An exception indicating that there are no states available.
     */
    class NoStatesLeftException : IllegalStateException()

    /**
     * Pops a state off the stack and notifies the listeners.
     */
    fun pop() {
        internalStack.removeAt(internalStack.lastIndex)
        if (internalStack.isEmpty()) throw NoStatesLeftException()
        val previous = internalStack.last()
        listeners.forEach { it.invoke(previous) }
    }

    /**
     * Pops a state off the stack and notifies the listeners.
     * If there are no states that can be popped off, the function returns false.
     */
    fun popOrFalse(): Boolean {
        return if (stack.size > 1) {
            pop()
            true
        } else false
    }

    /**
     * Pops all of the states off the stack except for the bottom one and notifies the listeners.
     */
    fun root() {
        val element = internalStack.first()
        internalStack.clear()
        internalStack.add(element)
        listeners.forEach { it.invoke(element) }
    }

    /**
     * Pops states off the stack until a predicate is satisfied and notifies the listeners.
     */
    fun back(predicate: (T) -> Boolean) {
        val index = internalStack.indexOfLast(predicate)
        while (index + 1 < internalStack.size) {
            internalStack.removeAt(index + 1)
        }
        listeners.forEach { it.invoke(internalStack.last()) }
    }

    /**
     * Clears the stack and starts over with a new element and notifies the listeners.
     */
    fun reset(element: T) {
        internalStack.clear()
        internalStack.add(element)
        listeners.forEach { it.invoke(value) }
    }

    fun withSize() = this.transform { it to this.stack.size }
}