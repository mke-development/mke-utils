# Ktor Extensions (`ktor-extensions`)

Модуль вспомогательных расширений для Ktor.

## Основной API

### `Attributes.findOrSet(key, value)`

Позволяет безопасно получить существующее значение из `Attributes` либо вычислить его один раз с помощью лямбды, сохранить в `Attributes` и вернуть. Если переданная лямбда возвращает `null`, значение не сохраняется.

```kotlin
import io.ktor.util.AttributeKey
import team.mke.utils.ktor.ext.findOrSet

val userIdKey = AttributeKey<Long>("userId")

val userId = call.attributes.findOrSet(userIdKey) {
    calculateUserId(call)
}
```

### `Route.root()`

Рекурсивно поднимается по иерархии маршрутизации `Route` до самого верхнего (корневого) роута и возвращает пару `Pair<Route, List<Route>>`:
1. Корневой `Route` (`parent == null`).
2. Список промежуточных и целевого узлов маршрутизации в порядке от корня к текущему узлу.

```kotlin
import team.mke.utils.ktor.ext.root

val (rootRoute, routePath) = route.root()
```

### Другие утилиты

- `ApplicationCall.ip`: извлечение клиентского IP (заголовок `X-Real-IP` или `request.origin.remoteHost`).
- `Parameters.get<T>(param)`: типизированное извлечение параметров запроса.
- `Parameters.date(name, formatter)` / `Parameters.dateOrFail(...)`: парсинг `LocalDate`.
- `PartData.FileItem.prepareFile(parentPath, fallbackExtension, nextName)`: подготовка уникального файла на диске.
- `RoutingResponse.contentDispositionHeader(filename)`: установка заголовка `Content-Disposition`.
