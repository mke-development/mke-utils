package team.mke.utils.model

fun <T> ofp(value: T): OptionalField<T> = OptionalField.Present(value)