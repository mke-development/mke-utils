package team.mke.utils.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import ru.raysmith.utils.format

// TODO use BigDecimal instead ?
fun Table.roundedDouble(name: String, decimal: Int): Column<Double> = registerColumn(name, RoundedDoubleColumnType(decimal))

class RoundedDoubleColumnType(val decimal: Int) : ColumnType<Double>() {
    override fun nonNullValueToString(value: Double): String {
        return value.format(decimal)
    }

    override fun notNullValueToDB(value: Double): Any {
        return value.format(decimal).toDouble()
    }

    override fun sqlType(): String = currentDialect.dataTypeProvider.doubleType()
    override fun valueFromDB(value: Any): Double = when (value) {
        is Double -> value
        is Number -> value.toDouble()
        is String -> value.toDouble()
        else -> error("Unexpected value of type Double: $value of ${value::class.qualifiedName}")
    }
}