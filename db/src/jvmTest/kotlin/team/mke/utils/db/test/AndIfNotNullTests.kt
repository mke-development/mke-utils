package team.mke.utils.db.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jetbrains.exposed.v1.core.AndOp
import org.jetbrains.exposed.v1.core.Op
import team.mke.utils.db.andIfNotNull

class AndIfNotNullTests : FreeSpec({
    val op1: Op<Boolean> = Op.TRUE
    val op2: Op<Boolean> = Op.FALSE

    "null receiver and null block returns null" {
        val receiver: Op<Boolean>? = null
        var blockCalled = false
        val res = receiver.andIfNotNull {
            blockCalled = true
            null
        }
        res.shouldBeNull()
        blockCalled shouldBe true
    }

    "null receiver and non-null block returns block result" {
        val receiver: Op<Boolean>? = null
        var blockCalled = false
        val res = receiver.andIfNotNull {
            blockCalled = true
            op1
        }
        res shouldBe op1
        blockCalled shouldBe true
    }

    "non-null receiver and null block returns receiver" {
        val receiver: Op<Boolean>? = op1
        var blockCalled = false
        val res = receiver.andIfNotNull {
            blockCalled = true
            null
        }
        res shouldBe op1
        blockCalled shouldBe true
    }

    "non-null receiver and non-null block returns AndOp" {
        val receiver: Op<Boolean>? = op1
        var blockCalled = false
        val res = receiver.andIfNotNull {
            blockCalled = true
            op2
        }
        res.shouldBeInstanceOf<AndOp>()
        blockCalled shouldBe true
    }
})
