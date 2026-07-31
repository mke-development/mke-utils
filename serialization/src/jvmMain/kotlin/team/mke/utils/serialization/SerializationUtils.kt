package team.mke.utils.serialization

import kotlinx.serialization.SerialName
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

inline fun <reified A : Annotation> Enum<*>.getEnumFieldAnnotation(): A? =
    javaClass.getDeclaredField(name).getAnnotation(A::class.java)

fun Enum<*>.getSerialName(): String = getEnumFieldAnnotation<SerialName>()?.value ?: name

fun KCallable<*>.getSerialName() = findAnnotation<SerialName>()?.value ?: name
fun KClass<*>.getSerialName() = findAnnotation<SerialName>()?.value ?: simpleName
