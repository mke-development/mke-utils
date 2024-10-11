package team.mke.utils.mail

import team.mke.utils.mail.message.MailMessage
import javax.mail.Authenticator
import javax.mail.Folder
import javax.mail.search.SearchTerm

interface IMail {
    fun folders(): Array<Folder>
    fun messages(folder: Folder, searchTerm: SearchTerm? = null): List<MailMessage>
    fun connect(authenticator: Authenticator? = null)
    fun disconnect()
}

inline fun <T, M : IMail> M.connect(block: M.() -> T): T {
    connect()
    return block().also {
        disconnect()
    }
}