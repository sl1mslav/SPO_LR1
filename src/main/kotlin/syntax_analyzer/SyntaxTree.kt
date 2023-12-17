package org.example.syntax_analyzer

class SyntaxTree {

    data class Node(
        val value: String = "E",
        val children: MutableList<Node> = mutableListOf()
    ) {
        fun addNode(node: Node) {
            children.add(node)
        }
    }
}