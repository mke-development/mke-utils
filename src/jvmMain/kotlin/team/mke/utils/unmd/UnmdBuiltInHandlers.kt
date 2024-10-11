package team.mke.utils.unmd

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

object UnmdBuiltInHandlers {
    val modelsHumanReadErrors = object : UnmdHandler {
        override fun process(node: ASTNode, text: String): String {
            return when (node.type) {
                MarkdownTokenTypes.TEXT,
                MarkdownTokenTypes.WHITE_SPACE,
                MarkdownTokenTypes.EOL,
                MarkdownTokenTypes.CODE_LINE,
                MarkdownTokenTypes.CODE_FENCE_CONTENT -> node.getTextInNode(text).toString()
                MarkdownElementTypes.INLINE_LINK,
                MarkdownElementTypes.FULL_REFERENCE_LINK,
                MarkdownElementTypes.SHORT_REFERENCE_LINK,
                MarkdownElementTypes.AUTOLINK -> {
                    val linkTextNode = node.children.find { it.type == MarkdownElementTypes.LINK_TEXT }
                    linkTextNode?.let { process(it, text) } ?: ""
                }
                MarkdownElementTypes.LINK_TEXT,
                MarkdownElementTypes.LINK_LABEL -> {
                    node.children.joinToString("") { process(it, text) }
                }
                else -> node.children.joinToString("") { process(it, text) }
            }
        }
    }
}