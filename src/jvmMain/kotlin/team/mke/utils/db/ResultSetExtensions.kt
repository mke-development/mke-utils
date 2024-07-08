package team.mke.utils.db

import java.sql.ResultSet

fun ResultSet.forEach(block: (ResultSet) -> Unit) {
    while (next()) {
        block(this)
    }
}

inline fun <reified T> ResultSet.getId(): T = when(T::class) {
    Int::class -> getInt("id")
    Long::class -> getLong("id")
    String::class -> getString("id")
    else -> throw UnsupportedOperationException("Can't get id row of ${T::class.simpleName} type")
} as T