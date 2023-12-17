package org.example.syntax_analyzer

import org.example.lexical_analyzer.AnalyzerResult
import org.example.lexical_analyzer.Error
import org.example.lexical_analyzer.Lexeme
import org.example.lexical_analyzer.LexemeType

class SyntacticalAnalyzer {

    /**
     * @throws UnsupportedOperationException
     */
    fun analyze(source: List<AnalyzerResult>): SyntaxTree.Node {
        val errors = source.filterIsInstance<Error>()
        if (errors.isNotEmpty()) {
            reportError(
                prefix = "На этапе лексического анализа были найдены ошибки:\n",
                value = errors.joinToString("\n") { it.toString() }
            )
        }
        return analyzeSyntax(source.filterIsInstance<Lexeme>(), SyntaxTree.Node())
    }

    /**
     *   c := 1.15; { присваиваем переменной c значение 1.15 }
     *   a := c;
     *   b := 1;
     *   if a > b then
     *       if a = b then
     *          c := 1.32;
     *   else
     *       c := 15; { неуспешное выполнение условия }
     *   if b = a then
     *       c := 0.3;
     */
    private fun analyzeSyntax(sourceLexemes: List<Lexeme>, startNode: SyntaxTree.Node): SyntaxTree.Node {
        var lexemes = sourceLexemes
        while (lexemes.isNotEmpty()) {
            val delimiterIndex = lexemes.indexOfFirst { it.type == LexemeType.DELIMITER }
            if (delimiterIndex == -1) {
                reportError(value = "${lexemes.first().position}: начатое выражение не заканчивается разделителем <;>")
            }
            val lexemesBeforeDelimiter = lexemes.subList(0, delimiterIndex)
            if (lexemesBeforeDelimiter.none { it.type == LexemeType.CONDITIONAL_OPERATOR }) {
                // Если работаем не с условной конструкцией в коде
                startNode.addNode(analyzeExpression(lexemesBeforeDelimiter))
                lexemes = lexemes.drop(delimiterIndex + 1)
            } else {
                // Ищём законченную условную конструкцию и анализируем всё внутри
                val conditionalLexemes = locateConditionalBlock(lexemes)
                startNode.addNode(analyzeConditional(conditionalLexemes))
                lexemes = lexemes.drop(conditionalLexemes.lastIndex + 1)
            }
        }
        return startNode
    }

    private fun locateConditionalBlock(lexemes: List<Lexeme>): List<Lexeme> {
        val lastElse = lexemes.findLast { it.value == "else" }
        if (lastElse?.position != null) {
            return lexemes.subList(
                fromIndex = 0,
                toIndex = lexemes.indexOfFirst {
                    it.type == LexemeType.DELIMITER &&
                    it.position !== null &&
                    it.position > lastElse.position
                } + 1
            )
        } else {
            return lexemes.subList(
                fromIndex = 0,
                toIndex = lexemes.indexOfFirst {
                    it.type == LexemeType.DELIMITER
                } + 1
            )
        }
    }

    private fun analyzeExpression(lexemes: List<Lexeme>): SyntaxTree.Node {
        if (lexemes.size < 3) {
            reportError(value ="${lexemes.first().position}: незаконченное выражение")
        }
        if (lexemes.first().type != LexemeType.IDENTIFIER) {
            reportError(value = "${lexemes.first().position}: выражение должно начинаться с идентификатора")
        }
        val secondLexeme = lexemes[1]
        if (secondLexeme.type != LexemeType.ASSIGN_SIGN) {
            reportError(value = "${secondLexeme.position}: вместо ${secondLexeme.value} ожидалась операция присваивания")
        }
        val thirdLexeme = lexemes[2]
        if (thirdLexeme.type != LexemeType.IDENTIFIER && thirdLexeme.type != LexemeType.CONSTANT) {
            reportError(value = "${thirdLexeme.position}: переменной может присваиваться только другая переменная или константа")
        }
        if (lexemes.size > 3) {
            reportError(value = "${lexemes.last().position}: превышено максимальное количество лексем в выражении присваивания")
        }
        val firstNode = SyntaxTree.Node(
            children = mutableListOf(SyntaxTree.Node(value = lexemes.first().value))
        )
        val secondNode = SyntaxTree.Node(
            value = secondLexeme.value
        )
        val thirdNode = SyntaxTree.Node(
            children = mutableListOf(SyntaxTree.Node(value = thirdLexeme.value))
        )
        return SyntaxTree.Node(children = mutableListOf(firstNode, secondNode, thirdNode))
    }

    private fun analyzeConditional(lexemes: List<Lexeme>): SyntaxTree.Node {
        val conditionalNode = SyntaxTree.Node()
        if (lexemes.first().value != "if") {
            reportError(value = "${lexemes.first().position}: условная конструкция должна начинаться с if")
        }
        conditionalNode.addNode(SyntaxTree.Node(value = "if"))

        if (lexemes.none { it.value == "then" }) {
            reportError(value = "${lexemes.first().position}: за предикатом должно следовать ключевое слово then")
        }
        conditionalNode.addNode(analyzeComparison(lexemes.subList(1, lexemes.indexOfFirst { it.value == "then" })))
        conditionalNode.addNode(SyntaxTree.Node(value = "then"))

        if (lexemes.any { it.value == "else" }) {
            val conditionalBlock = lexemes.subList(
                fromIndex = lexemes.indexOfFirst { it.value == "then" } + 1,
                toIndex = lexemes.indexOfLast { it.value == "else" }
            )
            val innerConditionalNode = SyntaxTree.Node()
            conditionalNode.addNode(analyzeSyntax(conditionalBlock, innerConditionalNode))
            conditionalNode.addNode(SyntaxTree.Node(value = "else"))
            val outerElseBlock = lexemes.subList(
                fromIndex = lexemes.indexOfLast { it.value == "else" } + 1,
                toIndex = lexemes.lastIndex + 1
            )
            analyzeSyntax(outerElseBlock, conditionalNode)
        } else {
            val conditionalBlock = lexemes.subList(
                fromIndex = lexemes.indexOfFirst { it.value == "then" } + 1,
                toIndex = lexemes.lastIndex + 1
            )
            analyzeSyntax(conditionalBlock, conditionalNode)
        }
        return conditionalNode
    }

    private fun analyzeComparison(lexemes: List<Lexeme>): SyntaxTree.Node {
        if (lexemes.size != 3) {
            reportError(value ="${lexemes.first().position}: некорректная форма конструкции сравнения двух элементов")
        }
        if (lexemes.first().type != LexemeType.IDENTIFIER && lexemes.first().type != LexemeType.CONSTANT) {
            reportError(value = "${lexemes.first().position}: сравниваться должны идентификаторы или константы")
        }
        val secondLexeme = lexemes[1]
        if (secondLexeme.type != LexemeType.COMPARISON_SIGN) {
            reportError(value = "${secondLexeme.position}: вместо ${secondLexeme.value} ожидался знак сравнения")
        }
        val thirdLexeme = lexemes[2]
        if (thirdLexeme.type != LexemeType.IDENTIFIER && thirdLexeme.type != LexemeType.CONSTANT) {
            reportError(value = "${thirdLexeme.position}: сравниваться должны идентификаторы или константы")
        }
        val firstNode = SyntaxTree.Node(
            children = mutableListOf(SyntaxTree.Node(value = lexemes.first().value))
        )
        val secondNode = SyntaxTree.Node(
            value = secondLexeme.value
        )
        val thirdNode = SyntaxTree.Node(
            children = mutableListOf(SyntaxTree.Node(value = thirdLexeme.value))
        )
        return SyntaxTree.Node(children = mutableListOf(firstNode, secondNode, thirdNode))
    }

    private fun reportError(
        prefix: String = "Ошибка на позиции ",
        value: String
    ) {
        throw UnsupportedOperationException(prefix + value)
    }
}