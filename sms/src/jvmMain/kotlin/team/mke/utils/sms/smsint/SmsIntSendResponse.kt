package team.mke.utils.sms.smsint

import kotlinx.serialization.Serializable

@Serializable
data class SmsIntSendResponse(

    /** Стоимость */
    val price: Price,

    /** Количество отправленных сообщений */
    val successMessagesCount: Int,

    /** Количество неотправленных сообщений */
    val errorMessagesCount: Int,

    /** Список сообщений */
    val messages: List<SentMessage>
)