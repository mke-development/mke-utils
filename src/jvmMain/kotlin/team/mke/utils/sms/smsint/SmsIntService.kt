package team.mke.utils.sms.smsint

import team.mke.utils.sms.SmsApiResult
import team.mke.utils.sms.SmsService

object SmsIntService : SmsService {
    override suspend fun send(message: String, phone: String): SmsApiResult<*> {
        return SmsIntApi.Sms.send(phone, message)
    }

    override suspend fun voice(message: String, phone: String): SmsApiResult<*> {
        return SmsIntApi.Sms.voice(phone, message)
    }

}