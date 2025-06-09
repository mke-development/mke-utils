package team.mke.utils.env

// TODO should be deleted and replaced by di mocks
fun isTestEnvironment() = System.getProperty("IS_TEST_ENVIRONMENT")?.toBoolean() ?: false