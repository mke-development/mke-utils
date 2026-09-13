# Сериализация (`serialization`)

Модуль дополнительных сериализаторов для `kotlinx.serialization`.

## Основной API

### `NullStringSerializer<T>` и `BigDecimalOrNullStringSerializer`

`NullStringSerializer<T>` — абстрактный `KSerializer<T?>`, позволяющий десериализовать значение `null` как из реального JSON `null`, так и из строкового литерала `"null"` (например, если внешнее API или форма присылает `"null"` как строковое значение). Поддерживает флаг `ignoreCase` (по умолчанию `false`).

`BigDecimalOrNullStringSerializer` — готовая реализация для типа `BigDecimal?`:

```kotlin
import kotlinx.serialization.Serializable
import team.mke.utils.serialization.BigDecimalOrNullStringSerializer
import java.math.BigDecimal

@Serializable
data class PriceDTO(
    @Serializable(with = BigDecimalOrNullStringSerializer::class)
    val price: BigDecimal?
)
```

### Другие сериализаторы модуля

- `BigDecimalSerializer`: сериализация `BigDecimal` в виде числа с фиксированным или настраиваемым масштабом (`scale`).
- `BooleanAsIntSerializer`: сериализация булевых значений в `0` / `1`.
- `DurationAsLongInMillisSerializer`: сериализация `Duration` в миллисекунды.
- `EnumFallbackSerializer`: отказоустойчивая сериализация `enum` с дефолтным fallback-значением.
- `LocalDateSerializer` / `LocalDateTimeSerializer` / `LocalTimeSerializer` / `ZonedDateTimeSerializer`: форматированная сериализация Java Time типов.
- `KotlinxLocalDateSerializer`: сериализация `kotlinx.datetime.LocalDate`.
- `StringNullIfEmptySerializer`: сериализация пустых строк в `null`.
