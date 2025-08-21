package team.mke.utils.db.column

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.currentDialect

class DoubleWithoutPrecisionColumnType : ColumnType<Double>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.doubleType()
    override fun valueFromDB(value: Any): Double = when (value) {
        is Double -> value
        is Number -> value.toDouble()
        is String -> value.toDouble()
        else -> error("Unexpected value of type Double: $value of ${value::class.qualifiedName}")
    }
}