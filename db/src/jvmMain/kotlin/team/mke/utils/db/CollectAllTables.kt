package team.mke.utils.db

import org.jetbrains.exposed.sql.Table
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.raysmith.exposedoption.Options
import kotlin.reflect.full.hasAnnotation

fun collectAllTables(packageName: String): List<Table> {
    val configuration = ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage(packageName))
        .setScanners(Scanners.SubTypes.filterResultsBy { true })

    return Reflections(configuration)
        .getSubTypesOf(Table::class.java)
        .filter { it.packageName.startsWith(packageName) }
        .mapNotNull { it.kotlin.objectInstance }
        .filter { !it::class.hasAnnotation<TransientTable>() }
        .toMutableList().apply {
            add(Options)
        }
}