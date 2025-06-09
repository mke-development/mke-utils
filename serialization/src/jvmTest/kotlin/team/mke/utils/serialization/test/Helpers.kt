package team.mke.utils.serialization.test

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import team.mke.utils.crashinterceptor.impl.TestCrashInterceptor

val crashInterceptor = TestCrashInterceptor
val logger: Logger = LoggerFactory.getLogger("test")