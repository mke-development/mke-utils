package team.mke.utils.unmd

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

private val flavour = CommonMarkFlavourDescriptor()
private val parser get() = MarkdownParser(flavour)

fun unmd(
    str: String,
    handler: UnmdHandler = UnmdBuiltInHandlers.modelsHumanReadErrors,
    onResult: (String) -> String = { it.trim().replace("\n", " ") }
): String {
    val parsedTree = parser.buildMarkdownTreeFromString(str)
    return onResult(handler.process(parsedTree, str))
}