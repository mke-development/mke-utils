package team.mke.utils.unmd

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

private val flavour = CommonMarkFlavourDescriptor()
private val parser get() = MarkdownParser(flavour, true, CancellationToken.NonCancellable)

fun unmd(
    str: String,
    handler: UnmdHandler = UnmdBuiltInHandlers.modelsHumanReadErrors,
    onResult: (String) -> String = { it.trim().replace("\n", " ") }
): String {
    val parsedTree = parser.buildMarkdownTreeFromString(str as CharSequence)
    return onResult(handler.process(parsedTree, str))
}
