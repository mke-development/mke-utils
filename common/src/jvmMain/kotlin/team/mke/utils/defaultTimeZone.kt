package team.mke.utils

import team.mke.utils.env.env
import java.time.ZoneId

val defaultTimeZone by env("TIME_ZONE", ZoneId.systemDefault()) { ZoneId.of(it) }
