package team.mke.utils.db

/**
 * Annotation to specify the name of the entity, which is used in logs and error messages.
 * */
@Target(AnnotationTarget.CLASS)
annotation class EntityName(val name: String)

/**
 * Annotation to specify the internationalized name of the entity, which is used in logs and error messages.
 *
 * @param key The internationalization key for the entity name.
 * @param name The name of the entity for locale described by the [key].
 * @param gender The gender of the object to determine the ending of words in errors
 * */
@Repeatable
@Target(AnnotationTarget.CLASS)
annotation class I18nEntityName(val key: String, val name: String, val gender: Gender)
