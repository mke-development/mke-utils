# Common (`common`)

`common` — набор базовых интерфейсов и utility-расширений для модулей `mke-utils`.

Модуль мультиплатформенный:
- `commonMain` — общий API
- `jvmMain` — JVM-специфичные утилиты
- `jsMain` — JS/React-утилиты для `Props`

## Что внутри

- извлечение и нормализация телефонных номеров (`exportPhones`)
- форматирование размера в байтах (`Number.bytes`)
- удобные toggle-операции для коллекций (`/`, `/=`)
- базовые интерфейсы инициализации (`Initiable`, `BaseInitiable`, `InitiableWithArgs`)
- безопасный вызов с перехватом ошибок (`safe`) на JVM
- time zone и date/time форматтеры на JVM
- utility для `react.Props` на JS

## `commonMain` API

### Инициализация и версионирование

- `Versionable` — контракт с полем `version: Int`
- `Initiable` — базовый контракт `init()`
- `BaseInitiable` — хранит флаг `isInit` и реализует `AutoCloseable`
- `InitiableWithArgs<T>` — инициализация с аргументом `init(data: T)`

Пример:

```kotlin
class Service : InitiableWithArgs<String>() {
    override fun init(data: String) {
        super.init(data)
        println("Init with: $data")
    }
}
```

### Телефонные номера

- `PhoneFormat(countryCode, phoneLength, resultCountryCode = "+$countryCode")`
- `defaultExportPhonesFormats` — по умолчанию российские форматы (`7` и `8` -> `+7`)
- `String.exportPhones(vararg formats)` — извлекает номера из произвольного текста

Пример:

```kotlin
import team.mke.utils.ext.exportPhones

val phones = "Связь: +7 (999) 111-22-33 и 89995554433".exportPhones()
// ["+79991112233", "+79995554433"]
```

### Расширения коллекций

- `MutableCollection<T> /= value` — toggle: добавить, если нет; удалить, если есть
- `List<T> / value` — возвращает новый список с toggle-семантикой
- `Set<T> / value` — возвращает новый set с toggle-семантикой

Пример:

```kotlin
val base = listOf("a", "b")
val x = base / "c" // [a, b, c]
val y = x / "a"    // [b, c]
```

### Расширения чисел

- `Number.bytes` — человекочитаемое представление размера (`б`, `кб`, `мб`, ...)

Пример:

```kotlin
123.bytes   // "123 б"
2060.bytes  // "2 кб"
```

## `jvmMain` API

### Безопасный вызов

`safe(crashInterceptor, logger, tags, printStacktrace) { ... }`:
- возвращает результат блока или `null`
- `CancellationException` подавляется (возвращается `null`)
- остальные исключения отправляются в `CrashInterceptor`

Пример:

```kotlin
val result = safe(crashInterceptor, logger) {
    riskyOperation()
}
```

### Date/Time

- `defaultTimeZone` — берется из env `TIME_ZONE`, fallback: `ZoneId.systemDefault()`
- `utcZoneId = ZoneId.of("Z")`
- `yekaZoneId = ZoneId.of("+05:00")`
- форматтеры: `shortDateFormat`, `dateFormat`, `dateTimeFormat`, `shortDateTimeFormat`, `hoursFormat`, `minutesFormat`

### Дополнительные расширения

- `Duration.rand(factorial = 0.1)` — случайно изменяет длительность в диапазоне `±factorial`
- `List<BigDecimal>.sum()` — сумма через `sumOf`
- `Throwable.findCause(...)` / `findCause<T>()` — поиск причины по типу в цепочке `cause`

### Утилиты JVM

- `argsToProperties(args: Array<String>)` — переносит JVM args вида `key=value` в `System.setProperty`

## `jsMain` API

### Работа с `react.Props`

- `Props.other(vararg prop)` — возвращает объект props без указанных полей
- поддерживает вложенные пути через точку (пример: `"user.token"`)
- доступны операторы `Props.get` и `Props.set`

Пример:

```kotlin
val cleanProps = props.other("onClick", "user.secret")
```
