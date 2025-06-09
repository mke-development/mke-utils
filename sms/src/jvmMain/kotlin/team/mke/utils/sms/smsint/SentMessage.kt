package team.mke.utils.sms.smsint

import kotlinx.serialization.Serializable

@Serializable
data class SentMessage(

    /** Успех */
    val success: Boolean,

    /** Идентификатор сообщения */
    val id: String,

    /** Дата\время отправки сообщения */
    val dateTimeSend: String,

    /** Описание ошибки */
    val error: String? = null
)