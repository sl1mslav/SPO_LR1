package org.example

import org.example.lexical_analyzer.LexicalAnalyzer
import org.example.lexical_analyzer.AnalyzerResult
import org.example.syntax_analyzer.SyntacticalAnalyzer
import org.example.tables.BinaryTreeTable
import tables.SimpleRehashTable
import kotlin.time.measureTime


fun main() {
    // testTables()
    val sourceCode = """
        c := 1.15; { присваиваем переменной c значение 1.15 }
        a := c;
        b := 1;
        if a > b then
            if a = b then
               c := 1.32;
            else
               c := 1.12;
        else
            c := 15; { неуспешное выполнение условия }
        if 15 > 1.12 then
            c := 13; 
    """.trimIndent()
    val lexicalResults = analyzeLexemes(sourceCode)
    analyzeSyntax(lexicalResults).onSuccess {
        it.value
    }.onFailure {
        println(it.message)
    }
}

/**
 * ЛР3, Партилов Д.М., Вариант 3
 * Синтаксический анализатор
 * Входная грамматика:
 * S -> F;
 * F -> if E then T else F | if E then F | a:= a
 * T -> if E then T else T | a := a
 * E -> a < a | a > a | a = a
 */
private fun analyzeSyntax(lexicalResults: List<AnalyzerResult>) = runCatching {
    val syntaxAnalyzer = SyntacticalAnalyzer()
    syntaxAnalyzer.analyze(lexicalResults)
}

/**
 * ЛР2, Партилов Д.М., Вариант 3
 * Лексический анализатор
 * Входной язык содержит операторы условия типа if ... then ... else ...  и if ... then,
 * разделённые символом ; (точка с запятой). Операторы условия содержат идентификаторы, знаки сравнения <, >, =,
 * десятичные числа с плавающей точкой (в обычной и логарифм. форме), знак присваивания (:=)
 */
private fun analyzeLexemes(sourceCode: String): List<AnalyzerResult> {
    val lexicalAnalyzer = LexicalAnalyzer()
    return lexicalAnalyzer.analyze(sourceCode)
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