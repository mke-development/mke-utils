package team.mke.utils

// TODO move to raysmith utils
/** Sets system properties from jvm args */
fun argsToProperties(args: Array<String>) {
    args.forEach {
        if (it.contains("=")) {
            val (key, value) = it.split("=")
            System.setProperty(key, value)
        }
    }
}