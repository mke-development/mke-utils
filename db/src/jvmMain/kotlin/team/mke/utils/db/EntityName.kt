package team.mke.utils.db

@Target(AnnotationTarget.CLASS)
annotation class EntityName(val name: String)

@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class I18nEntityName(val key: String, val name: String)