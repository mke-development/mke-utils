package team.mke.utils

import team.mke.utils.env.env
import java.time.ZoneId

/**
 * Default time zone for the application. `ZoneId.systemDefault()` by default.
 * Can be overridden by setting the `TIME_ZONE` environment variable or property.
 * */
val defaultTimeZone by env("TIME_ZONE", ZoneId.systemDefault()) { ZoneId.of(it) }
