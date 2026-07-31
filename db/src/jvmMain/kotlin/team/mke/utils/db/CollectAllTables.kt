package team.mke.utils.db

import org.jetbrains.exposed.v1.core.Table
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.raysmith.exposedoption.Options
import kotlin.reflect.full.hasAnnotation

/**
 * Рефлексия для получения всех таблиц из пакетов и их подпакетов, чтобы не указывать их вручную при инициализации базы данных.
 *
 * @param packageNames пакеты, из которых нужно собрать таблицы.
 * */
fun collectAllTables(vararg packageNames: String): List<Table> {
    return packageNames.flatMap { packageName ->
        val configuration = ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageName))
            .setScanners(Scanners.SubTypes.filterResultsBy { true })

        Reflections(configuration)
            .getSubTypesOf(Table::class.java)
            .filter { it.packageName.startsWith(packageName) }
            .mapNotNull { it.kotlin.objectInstance }
            .filter { !it::class.hasAnnotation<TransientTable>() }
            .toMutableList().apply {
                add(Options)
            }
    }
}
