package team.mke.utils.mail.message

import java.util.*
import javax.mail.BodyPart
import javax.mail.Message
import javax.mail.Multipart

data class MailMessage(private val message: Message) {
    val subject: String = message.subject
    val receivedDate: Date = message.receivedDate

    val from = message.from.map {
        val str = it.toString()
        val index = str.lastIndexOf('<')
        MailFrom(str.substring(0, (index - 1).coerceAtLeast(0)).trim(), str.substring(index + 1).dropLast(1))
    }

    val body = if (message.content is Multipart) {
        processMultipart(message.content as Multipart)
    } else { listOf(MailMessagePartData(MailMessagePartType.TEXT, message.content.toString())) }

    private fun processMultipart(multipart: Multipart): MutableList<MailMessagePartData> {
        val res = mutableListOf<MailMessagePartData>()

        for (i in 0 until multipart.count) {
            val bodyPart = multipart.getBodyPart(i)
            val data = processMessageBodyPart(bodyPart)
            if (data != null) {
                res.addAll(data)
            }
        }

        return res
    }

    private fun processMessageBodyPart(bodyPart: BodyPart) = when {
        bodyPart.contentType.contains("multipart") -> run {
            val multipart = bodyPart.content as Multipart
            processMultipart(multipart)
        }
        bodyPart.isMimeType("text/plain") -> listOf(MailMessagePartData(MailMessagePartType.TEXT, bodyPart.content))
        bodyPart.isMimeType("text/html") -> listOf(MailMessagePartData(MailMessagePartType.HTML, bodyPart.content))
        bodyPart.disposition != null && (bodyPart.disposition.equals(BodyPart.ATTACHMENT)) -> {
            listOf(MailMessagePartData(MailMessagePartType.ATTACHMENT, bodyPart.inputStream, bodyPart.fileName))
        }
        else -> null
    }
}