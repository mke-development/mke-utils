package team.mke.utils.mail.message

data class MailMessagePartData(val type: MailMessagePartType, val content: Any, val fileName: String? = null)