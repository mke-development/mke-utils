package team.mke.utils.ext

/**
 * If [other] is not in the collection, returns a new collection with [other] added.
 * If [other] is in the collection, returns a new collection with [other] removed
 * */
operator fun <T> MutableCollection<T>.divAssign(other: T) {
    if (contains(other)) {
        this.remove(other)
    } else {
        this.add(other)
    }
}

/**
 * If [other] is not in the list, returns a new list with [other] added.
 * If [other] is in the list, returns a new list with [other] removed
 * */
operator fun <T> List<T>.div(other: T): List<T> {
    return if (contains(other)) {
        this.minus(other)
    } else {
        this.plus(other)
    }
}

/**
 * If [other] is not in the list, returns a new list with [other] added.
 * If [other] is in the list, returns a new list with [other] removed
 * */
operator fun <T> Set<T>.div(other: T): Set<T> {
    return if (contains(other)) {
        this.minus(other)
    } else {
        this.plus(other)
    }
}