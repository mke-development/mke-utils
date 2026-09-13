# База данных и JSON (`db-json`)

Модуль интеграции `mke-utils-db` и библиотеки сериализации `kotlinx.serialization.json`.

## Область модуля

- Платформа: Kotlin Multiplatform (`jvmMain`)
- Зависимости: `team.mke:mke-utils-db`, `org.jetbrains.kotlinx:kotlinx-serialization-json`, `ru.raysmith:exposed-option`

## Основной API

### `jsonTransformer<Wrapped>(json = Json)`

Создает `ru.raysmith.exposedoption.Transformer<String, Wrapped>` для автоматической сериализации и десериализации объектов при сохранении в базу через свойства `option`:

```kotlin
import ru.raysmith.exposedoption.option
import team.mke.utils.db.json.jsonTransformer

@Serializable
data class MyConfig(val enabled: Boolean, val count: Int)

var config by option<MyConfig>(
    "MY_CONFIG",
    Duration.INFINITE,
    transformer = jsonTransformer(),
) {
    getOrSet(MyConfig(enabled = true, count = 10))
}
```
