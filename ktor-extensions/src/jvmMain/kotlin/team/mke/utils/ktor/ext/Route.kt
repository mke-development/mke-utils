package team.mke.utils.ktor.ext

import io.ktor.server.routing.Route

/**
 * Находит корневой [Route] и возвращает пару: корневой маршрут и список промежуточных
 * маршрутов от первого дочернего узла корня до текущего узла включительно.
 *
 * @param prev внутренний список для рекурсивного накопления пути
 * @return [Pair], где первое значение — корневой [Route], второе — список маршрутов от корня к `this`
 */
tailrec fun Route.root(prev: List<Route> = emptyList()): Pair<Route, List<Route>> {
    val p = parent
    return if (p == null) this to prev.reversed()
    else p.root(prev + this)
}
