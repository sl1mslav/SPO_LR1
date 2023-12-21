package org.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.core.node.Branch
import cafe.adriel.bonsai.core.node.Leaf
import cafe.adriel.bonsai.core.tree.Tree
import cafe.adriel.bonsai.core.tree.TreeScope
import org.example.lexical_analyzer.AnalyzerResult
import org.example.lexical_analyzer.LexicalAnalyzer
import org.example.semantic_analyzer.SemanticAnalyzer
import org.example.syntax_analyzer.Node
import org.example.syntax_analyzer.SyntacticalAnalyzer
import org.example.tables.BinaryTreeTable
import tables.SimpleRehashTable
import kotlin.time.measureTime


/*fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Партилов Д.М., ИВТ-424, ЛР3",
        state = rememberWindowState(width = 1000.dp, height = 1000.dp)
    ) {
        MaterialTheme {
            lr3()
        }
    }
}*/

fun main() {
    lr4()
}

fun lr4() {
    val sourceCode = """
        a := 1.2;
        c := a;
        c := 1.2;
        y := c;
        if a < 3 then
            if 4 > 2 then
                c := 35;
        else
            c := 32;
        if 9 > y then
            y := 3;
        if 9 > y then
            y := 3; 
    """.trimIndent()
    val semanticAnalyzer = SemanticAnalyzer()
    val lexicalResults = analyzeLexemes(sourceCode)
    val syntaxGraph = analyzeSyntax(lexicalResults).getOrThrow()

    val initialResults = semanticAnalyzer.analyzeNode(syntaxGraph)
    println("Исходные триады:")
    initialResults.forEachIndexed { index, triad ->
        println("${index + 1}) $triad")
    }
    println("\n ------------------------------------- \n")

    val reducedTriads = semanticAnalyzer.reduce(initialResults)
    println("Триады после свёртки:")
    reducedTriads.forEachIndexed { index, triad ->
        println("${index + 1}) $triad")
    }
    println("\n ------------------------------------- \n")

    println("Триады после анализа лишних операций: ")
    val optimizedTriads = semanticAnalyzer.optimize(reducedTriads)
    optimizedTriads.forEachIndexed { index, triad ->
        println("${index + 1}) $triad")
    }
    println("\n ------------------------------------- \n")

    println("Триады после исключения лишних операций: ")
    optimizedTriads.filter { it.operator.value != "SAME" }.forEachIndexed { index, triad ->
        println("${index + 1}) $triad")
    }
}

@Composable
fun lr3() {
    var sourceCode by remember {
        mutableStateOf("""
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
        """.trimIndent())
    }
    var graphResult: Result<Node>? by remember { mutableStateOf(null) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = sourceCode,
            onValueChange = {
                sourceCode = it
            }
        )
        Button(
            modifier = Modifier.align(Alignment.End).padding(10.dp),
            onClick = {
                val lexicalResults = analyzeLexemes(sourceCode)
                graphResult = analyzeSyntax(lexicalResults)
            },
            content = {
                Text("Проанализировать")
            }
        )
        graphResult?.onSuccess {
            drawNode(it)
        }?.onFailure {
            Text(text = it.message ?: "Произошла неизвестная ошибка")
        }
    }
}

@Composable
fun TreeScope.drawBranch(node: Node) {
    if (node.children.isEmpty()) {
        Leaf(node.lexeme?.value ?: "E")
    } else {
        Branch(node.lexeme ?: "E") {
            node.children.forEach { drawBranch(it) }
        }
    }
}

@Composable
fun drawNode(node: Node) {
    val tree = Tree<String> {
        drawBranch(node)
    }
    Bonsai(tree = tree)
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
    val timeToFillSimpleRehashTable =
        measureTime { identifiers.forEach { simpleRehashTable.insertElement(it) } }.inWholeMicroseconds
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