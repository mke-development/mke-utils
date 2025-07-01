package team.mke.utils.io

import java.io.ByteArrayOutputStream
import java.io.PrintStream

val originalOut = System.out
val disabledOutputStream = object : PrintStream(ByteArrayOutputStream()) {
    override fun write(b: Int) {

    }
}

fun blockOut() = OutBlocker().apply { block() }
fun blockOut(block: () -> Unit) = blockOut().use { block() }