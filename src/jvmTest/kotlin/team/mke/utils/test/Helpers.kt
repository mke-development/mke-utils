package team.mke.utils.test

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import team.mke.utils.crashinterceptor.TestCrashInterceptor

val crashInterceptor = TestCrashInterceptor
val logger: Logger = LoggerFactory.getLogger("test")