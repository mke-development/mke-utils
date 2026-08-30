package team.mke.utils.i18n

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Locale
import java.util.MissingResourceException

class UtilsTest : FunSpec({

    val en = Locale.ENGLISH
    val ru = Locale.forLanguageTag("ru")

    val enContext = object : LocaleContext {
        override val locale: Locale = en
    }

    val ruContext = object : LocaleContext {
        override val locale: Locale = ru
    }

    test("i18n with explicit locale should format simple messages") {
        i18n("simple", en) shouldBe "Simple message"
        i18n("simple", ru) shouldBe "Простое сообщение"
    }

    test("i18n with explicit locale should format messages with arguments") {
        i18n("greeting", en, "Alice") shouldBe "Hello Alice!"
        i18n("greeting", ru, "Алиса") shouldBe "Привет, Алиса!"
    }

    test("i18n with explicit locale should format messages with multiple arguments") {
        i18n("user_info", en, "Alice", 42, "ADMIN") shouldBe "User Alice (id: 42) has role ADMIN"
        i18n("user_info", ru, "Алиса", 42, "АДМИНИСТРАТОР") shouldBe "Пользователь Алиса (id: 42) имеет роль АДМИНИСТРАТОР"

        // Multiple arguments combined with ICU plural formatting
        i18n("order_status", en, "1001", "Alice", 1) shouldBe "Order #1001 by Alice: 1 item"
        i18n("order_status", en, "1002", "Bob", 3) shouldBe "Order #1002 by Bob: 3 items"

        i18n("order_status", ru, "1001", "Алиса", 1) shouldBe "Заказ #1001 для Алиса: 1 товар"
        i18n("order_status", ru, "1002", "Борис", 3) shouldBe "Заказ #1002 для Борис: 3 товара"
        i18n("order_status", ru, "1003", "Виктор", 5) shouldBe "Заказ #1003 для Виктор: 5 товаров"
    }

    test("i18n with explicit locale should format plural rules correctly") {
        // English plurals (one / other)
        i18n("items_count", en, 1) shouldBe "1 item"
        i18n("items_count", en, 2) shouldBe "2 items"
        i18n("items_count", en, 5) shouldBe "5 items"

        // Russian plurals (one / few / other)
        i18n("items_count", ru, 1) shouldBe "1 элемент"
        i18n("items_count", ru, 2) shouldBe "2 элемента"
        i18n("items_count", ru, 5) shouldBe "5 элементов"
        i18n("items_count", ru, 21) shouldBe "21 элемент"
    }

    test("i18n with explicit locale should return key when resource is missing") {
        i18n("unknown.key", en) shouldBe "unknown.key"
        i18n("unknown.key", ru) shouldBe "unknown.key"
    }

    test("i18n with LocaleContext should format messages in context locale") {
        with(enContext) {
            i18n("simple") shouldBe "Simple message"
            i18n("greeting", "Bob") shouldBe "Hello Bob!"
            i18n("items_count", 1) shouldBe "1 item"
            i18n("items_count", 3) shouldBe "3 items"
            i18n("missing.key") shouldBe "missing.key"
        }

        with(ruContext) {
            i18n("simple") shouldBe "Простое сообщение"
            i18n("greeting", "Боб") shouldBe "Привет, Боб!"
            i18n("items_count", 1) shouldBe "1 элемент"
            i18n("items_count", 3) shouldBe "3 элемента"
            i18n("missing.key") shouldBe "missing.key"
        }
    }

    test("i18nUnsafe with explicit locale should return value when key exists") {
        i18nUnsafe("simple", en) shouldBe "Simple message"
        i18nUnsafe("greeting", ru, "Чарли") shouldBe "Привет, Чарли!"
    }

    test("i18nUnsafe with explicit locale should throw MissingResourceException when key is missing") {
        shouldThrow<MissingResourceException> {
            i18nUnsafe("missing.key", en)
        }
    }

    test("i18nUnsafe with LocaleContext should return value when key exists") {
        with(enContext) {
            i18nUnsafe("simple") shouldBe "Simple message"
            i18nUnsafe("greeting", "David") shouldBe "Hello David!"
        }

        with(ruContext) {
            i18nUnsafe("simple") shouldBe "Простое сообщение"
            i18nUnsafe("greeting", "Давид") shouldBe "Привет, Давид!"
        }
    }

    test("i18nUnsafe with LocaleContext should throw MissingResourceException when key is missing") {
        with(enContext) {
            shouldThrow<MissingResourceException> {
                i18nUnsafe("missing.key")
            }
        }
    }

    test("I18n.addBundle should resolve keys from newly added bundles") {
        I18n.addBundle("custom.custom")

        i18n("custom.greeting", en, "Developer") shouldBe "Custom Hello Developer!"
        i18nUnsafe("custom.greeting", en, "Developer") shouldBe "Custom Hello Developer!"
    }
})
