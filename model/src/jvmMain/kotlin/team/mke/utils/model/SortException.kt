package team.mke.utils.model

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class SortException(field: String, allowedFields: List<String>) : IllegalArgumentException() {
    override val message: String =
        "Sorting by field '$field' is not possible. Allowed fields for sorting: ${allowedFields.joinToString(", ")}"

    constructor(field: String, dto: KClass<*>) : this(field, collect(dto))

    companion object {
        private val classType = typeOf<Class<*>>()
        private fun collect(dto: KClass<*>): List<String> {
            val result = mutableListOf<String>()
             dto.declaredMemberProperties
                .filter { it.hasAnnotation<Sortable>() }
                .forEach { s ->
                    if (s.returnType.isSubtypeOf(classType)) {
                        collect(s.returnType.classifier as KClass<*>).forEach {
                            result.add("${s.name}.$it")
                        }
                    } else {
                        result.add(s.name)
                    }
                }

            return result
        }
    }
}
