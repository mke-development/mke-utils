package team.mke.utils.env

internal fun throwMissingEnv(key: String): Nothing = error("Required environment variable `$key` is missing")
private fun getenv(key: String): String? = System.getProperty(key) ?: System.getenv(key)

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, возвращает [default]
 * */
@Suppress("UNCHECKED_CAST")
fun <T> env(key: String, default: T) = env(key, default) { it as T }

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, возвращает [default]
 *
 * @param transform лямбда приведения строкового значения к [T]
 * */
@Suppress("UNCHECKED_CAST")
fun <T> env(key: String, default: T, transform: (String) -> T = { it as T }) = Env(key, false) {
    transform((getenv(it) ?: return@Env default)) ?: default
}

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, возвращает null
 *
 * @param transform лямбда приведения строкового значения к [T]
 * */
@Suppress("UNCHECKED_CAST")
fun <T> env(key: String, transform: (String) -> T? = { it as T? }) = Env(key, false) {
    transform((getenv(it) ?: return@Env null))
}

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, возвращает null
 * */
fun env(key: String) = Env(key, false) { getenv(it) }

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, выбрасывает [IllegalStateException]
 * */
fun envRequired(key: String) = Env(key, true) { getenv(it) ?: throwMissingEnv(key) }

/**
 * Возвращает делегат со значением параметром запуска или переменной окружения. У параметра выше приоритет.
 * Если значение не установлено, выбрасывает [IllegalStateException]
 *
 * @param transform лямбда приведения строкового значения к [T]
 * */
@Suppress("UNCHECKED_CAST")
fun <T> envRequired(key: String, transform: (String) -> T = { it as T }) = Env(key, false) {
    transform(getenv(it) ?: throwMissingEnv(it))
}