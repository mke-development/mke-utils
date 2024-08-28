package team.mke.utils.sms.smsint

import kotlinx.serialization.Serializable

@Serializable
data class Message(val recipient: String, val text: String)

@Serializable
data class SmsIntSendBody(val messages: List<Message>)