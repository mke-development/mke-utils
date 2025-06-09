package team.mke.utils.unmd.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import team.mke.utils.unmd.unmd

class UnmdTests : FreeSpec({
    "unmd" {
        val src = "Some *Markdown*"
        unmd(src) shouldBe "Some Markdown"
    }
})