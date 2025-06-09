package team.mke.utils.mail.test

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import ru.raysmith.utils.notNull
import ru.raysmith.utils.toDate
import team.mke.utils.env.env
import team.mke.utils.mail.Mail
import team.mke.utils.mail.Protocol
import team.mke.utils.mail.connect
import java.time.LocalDateTime
import javax.mail.search.AndTerm
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm

class MailTests : FreeSpec({

    val protocol by env("TESTS_MAIL_PROTOCOL", Protocol.IMAPS) { Protocol.valueOf(it) }
    val login by env("TESTS_MAIL_LOGIN")
    val password by env("TESTS_MAIL_PASSWORD")
    val host by env("TESTS_MAIL_HOST", "imap.gmail.com")
    val port by env("TESTS_MAIL_PORT", 993) { it.toInt() }
    val folder by env("TESTS_MAIL_FOLDER", "INDEX")

    val mail by lazy { Mail(protocol, login!!, password!!, host, port, debug = true, folder) }

    val term = AndTerm(listOfNotNull(
        ReceivedDateTerm(ComparisonTerm.GE, LocalDateTime.now().minusDays(7).toDate()),
    ).toTypedArray())

    "mail".config(enabled = login notNull password) {
        shouldNotThrowAny {
            mail.connect {
                folders()
                messages(defaultFolder, term).size
            }
        }

        mail.isConnect shouldBe false

        shouldNotThrowAny {
            mail.connect {
                folders()
                messages(defaultFolder, term).size
            }
        }
    }
})