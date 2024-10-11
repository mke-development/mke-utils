package team.mke.utils.unmd

import org.intellij.markdown.ast.ASTNode

interface UnmdHandler {
    fun process(node: ASTNode, text: String): String
}