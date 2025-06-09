package team.mke.utils.mail

import javax.mail.Folder
import javax.mail.Message
import javax.mail.MethodNotSupportedException
import javax.mail.Store

class NotExistedFolder(store: Store, private val defaultFolderName: String) : Folder(store) {

    override fun getName() = defaultFolderName
    override fun getFullName() = defaultFolderName

    override fun close(expunge: Boolean) {
        return
    }

    override fun isOpen(): Boolean {
        return false
    }

    override fun exists(): Boolean {
        return false
    }

    override fun hasNewMessages(): Boolean {
        return false
    }

    override fun getParent() = throw MethodNotSupportedException("Cannot get parent folder from not existed Folder")
    override fun list(pattern: String?) = throw MethodNotSupportedException("Cannot list sub folders from not existed Folder")
    override fun getSeparator() = throw MethodNotSupportedException("Cannot get separator from not existed Folder")
    override fun getType() = throw MethodNotSupportedException("Cannot get type of not existed Folder")
    override fun create(type: Int) = throw MethodNotSupportedException("Cannot create not existed Folder")
    override fun getFolder(name: String?) = throw MethodNotSupportedException("Cannot get sub folders from not existed Folder")
    override fun delete(recurse: Boolean) = throw MethodNotSupportedException("Cannot delete not existed Folder")
    override fun renameTo(f: Folder?) = throw MethodNotSupportedException("Cannot rename not existed Folder")
    override fun open(mode: Int) = throw MethodNotSupportedException("Cannot open not existed Folder")
    override fun getPermanentFlags() = throw MethodNotSupportedException("Cannot get permanent flags from not existed Folder")
    override fun getMessageCount() = throw MethodNotSupportedException("Cannot get message count from not existed Folder")
    override fun getMessage(msgnum: Int) = throw MethodNotSupportedException("Cannot get message from not existed Folder")
    override fun appendMessages(msgs: Array<out Message?>) = throw MethodNotSupportedException("Cannot append to not existed Folder")
    override fun expunge() = throw MethodNotSupportedException("Cannot expunge not existed Folder")
}