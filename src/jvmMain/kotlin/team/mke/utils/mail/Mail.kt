package team.mke.utils.mail

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import team.mke.utils.ktor.NotFoundException
import team.mke.utils.mail.message.MailMessage
import java.util.*
import javax.mail.Authenticator
import javax.mail.Folder
import javax.mail.FolderClosedException
import javax.mail.FolderNotFoundException
import javax.mail.Session
import javax.mail.Store
import javax.mail.Transport
import javax.mail.internet.MimeMessage
import javax.mail.search.SearchTerm

// https://javaee.github.io/javamail/docs/api/javax/mail/package-summary.html
class Mail(
    val protocol: Protocol,
    private val login: String,
    private val password: String,
    val host: String,
    val port: Int,
    val debug: Boolean = false,
    private val defaultFolderName: String = "INBOX",
    val logger: Logger = defaultLogger
) : IMail {
    companion object {
        val defaultLogger: Logger = LoggerFactory.getLogger("mail")
    }

    private val props = Properties().apply {
        this["mail.store.protocol"] = protocol.value
        this["mail.${protocol.value}.host"] = host
        this["mail.${protocol.value}.port"] = port
        this["mail.debug"] = debug
        this["mail.debug.auth"] = debug
    }

    fun properties(block: Properties.() -> Unit) {
        props.apply(block)
    }

    var isConnect: Boolean = false
        private set

    private lateinit var session: Session
    private lateinit var store: Store

    override fun folders(): Array<Folder> = store.defaultFolder.list()

    override fun findFolder(name: String) = folders().find { it.name == name }

    override val defaultFolder by lazy { folders().find { it.name == defaultFolderName }
        ?: throw NotFoundException("Default folder '$defaultFolderName' not found") }

    fun send(block: MimeMessage.() -> Unit) {
        val message = MimeMessage(session).apply(block)
        Transport.send(message)
    }

    override fun messages(folder: Folder, searchTerm: SearchTerm?): List<MailMessage> {
        var shouldBtClosed = false
        if (!folder.isOpen) {
            shouldBtClosed = true
            folder.open(Folder.READ_ONLY)
        }
        val messages = if (searchTerm == null) folder.messages else folder.search(searchTerm)
        return messages.map { MailMessage(it) }.also {
            if (shouldBtClosed) {
                folder.close()
            }
        }
    }

    override fun connect(authenticator: Authenticator?) = synchronized(this) {
        session = Session.getDefaultInstance(props, authenticator)
        store = session.store
        store.connect(login, password)
        isConnect = true
    }

    override fun disconnect() = synchronized(this) {
        store.close()
        isConnect = false
    }
}