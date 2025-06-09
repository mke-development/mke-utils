package team.mke.utils.sms.smsint

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Price(
    @SerialName("currencyId") val currencyId: String,
    @SerialName("sum") val sum: Double
)