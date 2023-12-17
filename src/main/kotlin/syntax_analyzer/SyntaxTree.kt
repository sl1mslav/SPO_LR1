package org.example.syntax_analyzer

class SyntaxTree {

    private val rootNode = Node()

    data class Node(
        val value: String = "E",
        val children: MutableList<Node> = mutableListOf()
    ) {
        fun addNode(node: Node) {
            children.add(node)
        }
    }

    fun addUpperNode(node: Node) {
        rootNode.children.add(node)
    }

    fun get() = rootNode
}