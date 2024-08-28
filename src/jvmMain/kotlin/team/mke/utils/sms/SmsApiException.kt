package team.mke.utils.sms

import io.ktor.http.*

@Suppress("CanBeParameter")
class SmsApiException(
    val msg: String?, val httpCode: HttpStatusCode, val errorCode: Int? = null
) : RuntimeException("[${httpCode.value}] ${errorCode?.let { "#$it " } ?: ""}$msg")