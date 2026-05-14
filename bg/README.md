# Фоновые процессы (`bg`)

`bg` — JVM-модуль-утилита для запуска и управления фоновыми процессами на корутинах.

Он предоставляет:
- одноразовые задачи (`bg`)
- циклические задачи с задержкой (`bgWhile`)
- реестр процессов и вспомогательные методы жизненного цикла (`Background`)
- безопасную семантику перезапуска для активных задач
- хелпер расчета задержки для ежедневного запуска (`getDailyDelay`)

## Область модуля

- Платформа: JVM (только `jvmMain`)
- Базовый диспетчер: `Dispatchers.Default`
- Внутренний scope процессов: `Background.scope` (`SupervisorJob`)

## Основной API

### `bg(...)`
Создает фоновый процесс, который выполняется один раз.

```kotlin
import kotlinx.coroutines.delay
import team.mke.utils.bg.bg
import team.mke.utils.crashinterceptor.impl.DevCrashInterceptor

val process = bg(
    crashInterceptor = DevCrashInterceptor,
    name = "sync-cache"
) {
    // фоновая логика
    delay(1_000)
}
```

### `bgWhile(...)`
Создает циклический фоновый процесс.

Дополнительные параметры:
- `delay`: задержка между итерациями
- `maxIterations`: лимит итераций (`-1` означает без ограничений)

```kotlin
import kotlin.time.Duration.Companion.seconds
import team.mke.utils.bg.bgWhile
import team.mke.utils.crashinterceptor.impl.DevCrashInterceptor

val polling = bgWhile(
    crashInterceptor = DevCrashInterceptor,
    name = "poll-updates",
    startDelay = { 1.seconds },
    delay = { 5.seconds },
    maxIterations = -1
) {
    // Логика итерации
}
```

## Жизненный цикл и реестр

Глобальный реестр процессов управляется объектом `Background`.

Доступные операции:
- `Background.hasProcess(id)`
- `Background.findProcess(id)`
- `Background.processCount()`
- `Background.cancelAll()`
- `Background.restart(id)`

Операции процесса (`BaseBackgroundProcess` / `BackgroundProcess`):
- `start()` / `start(throwOnRegistered)`
- `stop()`
- `cancel()`
- `cancelAndJoin()`
- `join()`
- `restart()`
- `isActive`

### Дублирующиеся id

- Регистрация процесса с уже существующим активным `id` вызывает ошибку.
- После `stop()` тот же `id` можно зарегистрировать повторно.

## Поведение перезапуска

`restart()` учитывает текущее состояние:
- возвращает `true`, если перезапуск выполнен сразу
- возвращает `false`, если процесс сейчас активен и перезапуск поставлен в очередь

Отложенный перезапуск применяется после завершения текущей итерации/запуска.

## Обработка ошибок

Все ошибки времени выполнения делегируются переданному `CrashInterceptor`.

- Исключения внутри фоновых действий перехватываются.
- При необработанном падении корутины процесс удаляется из реестра `Background`.

## Хелпер для ежедневного расписания

### `getDailyDelay(lastInvocation, time, now = now())`

Возвращает задержку до ближайшей точки ежедневного запуска.

Ключевые особенности:
- если задача уже выполнялась сегодня, следующий запуск назначается на завтра в `time`
- если задача сегодня еще не выполнялась и процесс стартовал близко ко времени запуска, она может выполниться сегодня
- иначе запуск переносится на следующий день

### Переменная окружения

- `BG_DAILY_EVENT_DELAY_TOLERANCE` (секунды, по умолчанию `300`)
- задает допустимое окно после целевого ежедневного времени, в котором событие еще считается не пропущенным

## Примечания

- `BaseDailyBackgroundProcess` доступен как абстрактный хелпер для ежедневных задач.
- По умолчанию логирование использует `Background.logger` (`"bg"`).
- Контекст потока/корутины сохраняется и дополняется `LoggableCoroutineName`.
