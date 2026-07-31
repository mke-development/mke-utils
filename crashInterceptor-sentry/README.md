# Crash Interceptor: Sentry (`crashInterceptor-sentry`)

Модуль расширяет базовый интерфейс `CrashInterceptor` готовой реализацией для отправки ошибок и логов в Sentry.
## Возможности

- **Интеграция с Sentry SDK**: Отправляет перехваченные исключения (`intercept`) и сообщения (`message`) в Sentry.
- **Фильтрация исключений**:
  - Исключения из списка `blackList` (по умолчанию `CancellationException`, `SocketTimeoutException` и `SocketException`) игнорируются при отправке.
  - В не-production среде (проверяется через `Environment.isProd()`) ошибки только логируются локально, но не отправляются в Sentry.
- **Сквозной `log_id`**: Каждому перехваченному исключению генерируется уникальный `log_id` (UUID). Он прикрепляется как тег в Sentry, помещается в SLF4J `MDC`, что упрощает поиск инцидентов.
- **Поддержка кастомных тегов**: Все переданные теги сохраняются в конфигурации (Scope) Sentry.

## Настройка

Инициализация перехватчика должна происходить на старте приложения:

```kotlin
import team.mke.utils.crashinterceptor.impl.sentry.SentryCrashInterceptor

SentryCrashInterceptor.init(logger) {
    dsn = envRequired("SENTRY_DSN")

    // Исключения, которые не будут отправляться в Sentry
    blackList(IllegalArgumentException::class)

    // Дополнительная настройка Sentry SDK
    options {
        tracesSampleRate = 0.5
    }
}
```
