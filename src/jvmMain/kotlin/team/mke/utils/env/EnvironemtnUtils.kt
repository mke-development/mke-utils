package team.mke.utils.env

fun isTestEnvironment() = System.getProperty("IS_TEST_ENVIRONMENT")?.toBoolean() ?: false