package team.mke.utils.model

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

fun KClass<*>.collectSortableFields(): List<String> {
    val result = mutableListOf<String>()
    declaredMemberProperties
        .forEach { s ->
            if ((s.returnType.classifier as? KClass<*>)?.qualifiedName?.startsWith(this.java.`package`.name) == true) {
                (s.returnType.classifier as KClass<*>).collectSortableFields().forEach {
                    result.add("${s.name}.$it")
                }
            } else if (s.hasAnnotation<Sortable>()) {
                result.add(s.name)
            }
        }

    return result
}
