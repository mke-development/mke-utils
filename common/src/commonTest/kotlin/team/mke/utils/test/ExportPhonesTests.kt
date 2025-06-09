package team.mke.utils.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import team.mke.utils.ext.PhoneFormat
import team.mke.utils.ext.defaultExportPhonesFormats
import team.mke.utils.ext.exportPhones

class ExportPhonesTests : FreeSpec({
    "exportPhones with default formats" {
        "+71234567890".exportPhones() shouldBe listOf("+71234567890")
        "+7 (123) 456-78-90".exportPhones() shouldBe listOf("+71234567890")
        "+7123456789".exportPhones().shouldBeEmpty()
        "asd +71234567890 asd".exportPhones() shouldBe listOf("+71234567890")
        "asd +71234567890 asd 89876543210 asd 79876543211".exportPhones() shouldBe
                listOf("+71234567890", "+79876543210", "+79876543211")
    }

    "exportPhones with additional formats" {
        val rawString = "+996123456789, 996123456788, +996 (123) 45-67-87"
        val actual = rawString.exportPhones(*defaultExportPhonesFormats + PhoneFormat(996, 9))
        actual shouldBe listOf("+996123456789", "+996123456788", "+996123456787")
    }

    "exportPhones formats should not conflict with same country codes started and total length" {
        val rawString = "+996123456789, +997123456788"
        val actual = rawString.exportPhones(*defaultExportPhonesFormats + PhoneFormat(996, 9) + PhoneFormat(99, 10))
        actual shouldBe listOf("+996123456789", "+997123456788")
    }
})