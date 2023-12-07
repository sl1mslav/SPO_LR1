package org.example

import org.example.tables.BinaryTreeTable
import tables.SimpleRehashTable
import kotlin.time.measureTime

/**
 * ЛР1, Партилов Д.М., ИВТ-424Б, Вариант 3
 * Метод 1: Простое рехэширование
 * Метод 2: Бинарное дерево
 */
fun main() {
    val identifiers = readLinesFromInputStream(inputStream = getStreamFromResources(IDENTIFIERS_FILE_NAME))
    if (identifiers == null) {
        println("Не удалось прочесть файл. Завершаем исполнение программы...")
        return
    }

    // Создаём таблицы для дальнейшего заполнения
    val simpleRehashTable = SimpleRehashTable(TABLE_SIZE)
    val binaryTreeTable = BinaryTreeTable()

    // Измеряем время, необходимое для заполнения таблиц элементами из файла
    val timeToFillSimpleRehashTable = measureTime { identifiers.forEach { simpleRehashTable.insertElement(it) } }.inWholeMicroseconds
    val timeToFillBinaryTreeTable = measureTime { binaryTreeTable.fill(identifiers) }.inWholeMicroseconds
    println("Время, необходимое для заполнения таблицы \"Простое рехэширование\": $timeToFillSimpleRehashTable мкс")
    println("Время, необходимое для заполнения таблицы \"Бинарное дерево\": $timeToFillBinaryTreeTable мкс")

    // Тестируем поиск по таблице
    simpleRehashTable.testSearch()
    binaryTreeTable.testSearch(identifiers)

    // Пользовательский поиск
    while (true) {
        print("Введите строку для поиска, или последовательность 'STOP_PROGRAM', чтобы завершить выполнение: ")
        val input: String = readln()
        if (input == "STOP_PROGRAM") return
        val simpleRehashTableAttempts = simpleRehashTable.findElement(input)
        if (simpleRehashTableAttempts != null) {
            println("Количество попыток найти элемент для таблицы с простым рехешированием: $simpleRehashTableAttempts")
        }
        val binaryTreeTableAttempts = binaryTreeTable.findElement(input)
        if (binaryTreeTableAttempts != null) {
            println("Количество попыток найти элемент для бинарного дерева: $binaryTreeTableAttempts")
        }
        println()
    }
}

private const val TABLE_SIZE = 200
private const val IDENTIFIERS_FILE_NAME = "/identifiers.txt"