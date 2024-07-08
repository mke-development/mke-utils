package team.mke.utils.serialization

import kotlinx.serialization.SerialName

inline fun <reified A : Annotation> Enum<*>.getEnumFieldAnnotation(): A? =
    javaClass.getDeclaredField(name).getAnnotation(A::class.java)

fun Enum<*>.getSerialName(): String = getEnumFieldAnnotation<SerialName>()?.value ?: name