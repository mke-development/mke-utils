# База данных (`db`)

Модуль утилит и расширений для работы с реляционными базами данных на базе **Exposed**.

## Основной API

### Условные выражения (`andIfNotNull`, `andIf`, `orIf`, `orIfNotNull`)

Методы для гибкого и лаконичного построения SQL-условий `Op<Boolean>` без ручных проверок на `null`:

```kotlin
import team.mke.utils.db.andIfNotNull

var where: Op<Boolean>? = null

// Если лямбда возвращает null, условие не добавляется
where = where.andIfNotNull {
    filterValue?.let { Users.name eq it }
}
```

### Поиск и валидация сущностей

- `EntityClass.findByIdOrThrow(id, message, block)`: поиск сущности с выбросом `EntityNotFoundException`.
- `EntityClass.findByIdOrNull(id)`: поиск с автоматическим учетом `NotDeletableEntity`.
- `EntityClass.any(op)` / `EntityClass.none(op)`: проверка существования записей по предикату.

### Трансформации столбцов (`Transform.kt`)

- `Column<LocalDateTime>.transformToZonedDateTime(timeZoneId)`: автоматическое преобразование в `ZonedDateTime`.
- `Column<Double>.transformToBigDecimal(scale, roundingMode)`: преобразование в `BigDecimal`.

### Пагинация и сортировка

- `Query.andPaginationIfNotNull(...)`: интеграция курсорной пагинации.
- `GetAllImpl` / `GetAllImplWithPaginationData`: стандартная реализация выборок с пагинацией.
