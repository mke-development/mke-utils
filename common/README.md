# Базовые утилиты (`common`)

`common` — набор базовых интерфейсов и utility-расширений для модулей `mke-utils`.

Модуль мультиплатформенный:
- `commonMain` — общий API
- `jvmMain` — JVM-специфичные утилиты
- `jsMain` — JS/React-утилиты для `Props`

## Что внутри

- извлечение и нормализация телефонных номеров (`exportPhones`)
- форматирование размера в байтах (`Number.bytes`)
- удобные toggle-операции для коллекций (`/`, `/=`)
- безопасный вызов с перехватом ошибок (`safe`) на JVM
- time zone и date/time форматтеры на JVM
- utility для `react.Props` на JS

## `commonMain` API

### Телефонные номера

- `PhoneFormat(countryCode, phoneLength, resultCountryCode = "+$countryCode")`
- `defaultExportPhonesFormats` — по умолчанию российские форматы (`7` и `8` -> `+7`)
- `String.exportPhones(vararg formats)` — извлекает номера из произвольного текста

Пример:

```kotlin
import team.mke.utils.exportPhones

val phones = "Связь: +7 (999) 111-22-33 и 89995554433".exportPhones()
// ["+79991112233", "+79995554433"]
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

### Дата и время (Date/Time)

- `defaultTimeZone` — берет часовой пояс из переменной окружения `TIME_ZONE`, фоллбек: `ZoneId.systemDefault()`.
- `utcZoneId = ZoneId.of("Z")`
- `yekaZoneId = ZoneId.of("+05:00")`

Предустановленные форматы дат с русской локалью (`DateTimeFormatter`):
- `shortDateFormat` (`dd.MM.yyyy`) — пример: `15.05.2026`
- `dateFormat` (`d MMMM yyyy`) — пример: `15 мая 2026`
- `dateTimeFormat` (`d MMMM yyyy, HH:mm`) — пример: `15 мая 2026, 14:30`
- `shortDateTimeFormat` (`dd.MM.yyyy, HH:mm`) — пример: `15.05.2026, 14:30`
- `timeFormatter` (`HH:mm`) — пример: `14:30`
- `hoursFormat` (`HH`) — пример: `14`
- `minutesFormat` (`mm`) — пример: `30`

### Дополнительные расширения

- `Duration.rand(factorial = 0.1)` — случайно изменяет длительность в диапазоне `±factorial`
- `List<BigDecimal>.sum()` — сумма через `sumOf`
- `List<BigDecimal>.avg()` — среднее через `sum() / size`
- `Throwable.findCause(...)` / `findCause<T>()` — поиск причины по типу в цепочке `cause`

## `jsMain` API

### Работа с `react.Props`

- `Props.other(vararg prop)` — возвращает объект props без указанных полей
- поддерживает вложенные пути через точку (пример: `"user.token"`)
- доступны операторы `Props.get` и `Props.set`

Пример:

```js
const props = {
    onClick: () => console.log('clicked'),
    user: {
        name: 'Alice',
        secret: 's3cr3t'
    }
}
```

```kotlin
val cleanProps = props.other("onClick", "user.secret")
```

```js
// cleanProps 
{
    user: {
        name: 'Alice'
    }
}
```
