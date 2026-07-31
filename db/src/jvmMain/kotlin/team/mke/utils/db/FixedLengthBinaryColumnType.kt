package team.mke.utils.db

import org.jetbrains.exposed.v1.core.BasicBinaryColumnType
import org.jetbrains.exposed.v1.core.BinaryColumnType
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

open class FixedLengthBinaryColumnType(val length: Int) : BasicBinaryColumnType() {
    override fun sqlType(): String = "BINARY($length)"

    override fun validateValueBeforeUpdate(value: ByteArray?) {
        if (value is ByteArray) {
            val valueLength = value.size
            require(valueLength == length) {
                "Value can't be stored to database column because its length ($valueLength) doesn't match column length ($length)"
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BinaryColumnType

        return length == other.length
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + length
        return result
    }
}

fun Table.fixedLengthBinary(name: String, length: Int): Column<ByteArray> =
    registerColumn(name, FixedLengthBinaryColumnType(length))
