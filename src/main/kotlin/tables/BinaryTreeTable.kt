package org.example.tables

import kotlin.time.measureTime

class BinaryTreeTable {

    private var rootNode: Node? = null

    data class Node(
        val identifier: String,
        var left: Node? = null,
        var right: Node? = null
    ) {
        fun find(identifier: String, attempts: Int): Pair<Node?, Int>? = when {
            identifier < this.identifier -> left?.find(identifier, attempts + 1)
            identifier > this.identifier -> right?.find(identifier, attempts + 1)
            else -> this to attempts
        }

        fun insert(identifier: String) {
            if (identifier > this.identifier) {
                if (this.right == null) {
                    this.right = Node(identifier)
                } else {
                    this.right?.insert(identifier)
                }
            } else if (identifier < this.identifier) {
                if (this.left == null) {
                    this.left = Node(identifier)
                } else {
                    this.left?.insert(identifier)
                }
            }
        }
    }

    fun fill(identifiers: List<String>) {
        if (identifiers.isEmpty()) {
            println("Список идентификаторов пуст!")
            return
        }
        rootNode = Node(identifier = identifiers.first())
        identifiers.drop(1).forEach {
            rootNode?.insert(it)
        }
    }

    fun findElement(identifier: String): Int? {
        val result = rootNode?.find(identifier, attempts = 1)
        if (result?.first == null) {
            println("Не удалось найти этот идентификатор")
        }
        return result?.second
    }

    fun testSearch(identifiers: List<String>) {
        if (rootNode == null) {
            println("Корневой элемент пуст, выполнение теста невозможно")
            return
        }

        val anyElement = identifiers.random()

        println("Поиск элемента \"${anyElement}\"")
        val timeToFindElement = measureTime { findElement(anyElement) }.inWholeMicroseconds
        println("Время, затраченное на поиск: $timeToFindElement мкс")
    }
}