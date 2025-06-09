package team.mke.utils.sms

interface SmsService {
    suspend fun send(message: String, phone: String): SmsApiResult<*>
    suspend fun voice(message: String, phone: String): SmsApiResult<*>
}