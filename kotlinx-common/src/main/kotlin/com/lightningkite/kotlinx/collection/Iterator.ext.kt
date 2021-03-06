package com.lightningkite.kotlinx.collection

/**
 * Returns a wrapper that maps the iterator to have items of a different type.
 */
fun <S, T> MutableListIterator<S>.mapping(read: (S) -> T, write: (T) -> S): MutableListIterator<T> {
    return object : MutableListIterator<T> {
        override fun hasPrevious(): Boolean = this@mapping.hasPrevious()
        override fun nextIndex(): Int = this@mapping.nextIndex()
        override fun previous(): T = read(this@mapping.previous())
        override fun previousIndex(): Int = this@mapping.previousIndex()
        override fun add(element: T) = this@mapping.add(write(element))
        override fun hasNext(): Boolean = this@mapping.hasNext()
        override fun next(): T = read(this@mapping.next())
        override fun remove() = this@mapping.remove()
        override fun set(element: T) = this@mapping.set(write(element))
    }
}

/**
 * Returns a wrapper that maps the iterator to have items of a different type.
 */
fun <S, T> MutableIterator<S>.mappingMutable(read: (S) -> T): MutableIterator<T> {
    return object : MutableIterator<T> {
        override fun hasNext(): Boolean = this@mappingMutable.hasNext()
        override fun next(): T = read(this@mappingMutable.next())
        override fun remove() = this@mappingMutable.remove()
    }
}

/**
 * Returns a wrapper that maps the iterator to have items of a different type.
 */
fun <S, T> Iterator<S>.mapping(read: (S) -> T): Iterator<T> {
    return object : Iterator<T> {
        override fun hasNext(): Boolean = this@mapping.hasNext()
        override fun next(): T = read(this@mapping.next())
    }
}

/**
 * Returns a wrapper that maps the iterator to have items of a different type.
 */
fun <S, T> ListIterator<S>.mapping(read: (S) -> T): ListIterator<T> {
    return object : ListIterator<T> {
        override fun hasPrevious(): Boolean = this@mapping.hasPrevious()
        override fun nextIndex(): Int = this@mapping.nextIndex()
        override fun previous(): T = read(this@mapping.previous())
        override fun previousIndex(): Int = this@mapping.previousIndex()
        override fun hasNext(): Boolean = this@mapping.hasNext()
        override fun next(): T = read(this@mapping.next())
    }
}