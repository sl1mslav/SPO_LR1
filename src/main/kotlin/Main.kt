package org.example

import org.example.lexical_analyzer.Analyzer
import org.example.tables.BinaryTreeTable
import tables.SimpleRehashTable
import kotlin.time.measureTime


fun main() {
    // testTables()
    analyzeCode()
}

/**
 * ЛР2, Партилов Д.М., Вариант 3
 * Лексический анализатор
 * Входной язык содержит операторы условия типа if ... then ... else ...  и if ... then,
 * разделённые символом ; (точка с запятой). Операторы условия содержат идентификаторы, знаки сравнения <, >, =,
 * десятичные числа с плавающей точкой (в обычной и логарифм. форме), знак присваивания (:=)
 */
private fun analyzeCode() {
    val analyzer = Analyzer()
    analyzer.analyze("""
        program analyzator;
        a, b, c : integer;
        c := a - b + 15;
    """.trimIndent())
    analyzer.resultingLexemes.forEach {
        println("${it.value} - ${it.type.asString}")
    }
}

/**
 * ЛР1, Партилов Д.М., ИВТ-424Б, Вариант 3
 * Метод 1: Простое рехэширование
 * Метод 2: Бинарное дерево
 */
private fun testTables() {
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
    binaryTreeTable.testSearch()

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

private const val TABLE_SIZE = 2200
private const val IDENTIFIERS_FILE_NAME = "/identifiers2000.txt"