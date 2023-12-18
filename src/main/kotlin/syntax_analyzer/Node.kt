package org.example.syntax_analyzer

import org.example.lexical_analyzer.Lexeme


data class Node(
    val lexeme: Lexeme? = null,
    val children: MutableList<Node> = mutableListOf()
) {
    fun addNode(node: Node) {
        children.add(node)
    }
}