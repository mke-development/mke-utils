package team.mke.utils.sms

sealed class SmsApiResult<out T> {
    data class Success<T>(val response: T) : SmsApiResult<T>()
    data class Error(val exception: Exception?) : SmsApiResult<Nothing>()
}