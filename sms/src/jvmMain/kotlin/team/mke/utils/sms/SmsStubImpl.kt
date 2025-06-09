package team.mke.utils.sms

object SmsStubImpl : SmsService {
    override suspend fun send(message: String, phone: String): SmsApiResult<*> {
        logger.debug("Sms sent to $phone. Message: $message")
        return SmsApiResult.Success("stub")
    }
    override suspend fun voice(message: String, phone: String): SmsApiResult<*> {
        logger.debug("Voice sent to $phone. Message: $message")
        return SmsApiResult.Success("stub")
    }
}