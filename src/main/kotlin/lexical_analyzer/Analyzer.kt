package org.example.lexical_analyzer

class Analyzer {

    data class AnalyzerError(
        val index: Int,
        val value: String
    )

    private enum class State {
        START,
        IDENTIFIER,
        NUMBER,
        ASSIGN,
        COMMENT,
        /*ERROR,
        FINISH*/
    }

    private var currentState = State.START
    private var lexemeBuffer: String = ""
    private val resultingLexemes = mutableListOf<Lexeme>()
    private val errors = mutableListOf<AnalyzerError>()

    /**
     *         c := 1.15*;
     *         a := c;
     *         b := 1;
     *         if a > b then
     *             c := 2.3;
     *         else
     *             c := 15;
     *         if b = a then
     *             c := 0.3;
     */
    fun analyze(text: String) {
        val linesCount = text.lines().size
        text.forEachIndexed { index, char ->
            when (currentState) {
                State.START -> {
                    clearBuffer()
                    when {
                        char in listOf(' ', '\n', '\t', '\r') -> {}

                        char.isLetter() -> {
                            addToBuffer(char)
                            currentState = State.IDENTIFIER
                        }

                        char.isDigit() -> {
                            addToBuffer(char)
                            currentState = State.NUMBER
                        }

                        char == '{' -> {
                            if (text.substring(startIndex = index).contains('}')) {
                                currentState = State.COMMENT
                            } else {
                                reportError(index, "Незакрытый комментарий!")
                            }
                        }

                        char == ':' -> {
                            addToBuffer(char)
                            currentState = State.ASSIGN
                        }

                        else -> {
                            addToBuffer(char)
                            addLexeme(
                                index,
                                lexemeBuffer,
                                KNOWN_LEXEMES.find { it.value == lexemeBuffer }?.type ?: LexemeType.DELIMITER
                            )
                            currentState = State.START
                        }
                    }
                }

                State.IDENTIFIER -> {
                    if (char.isLetterOrDigit()) {
                        addToBuffer(char)
                    } else {
                        addLexeme(
                            index = index,
                            value = lexemeBuffer,
                            type = KNOWN_LEXEMES.find { it.value == lexemeBuffer }?.type ?: LexemeType.IDENTIFIER
                        )
                        if (!char.isWhitespace()) {
                            addLexeme(
                                index = index,
                                value = char.toString(),
                                type = KNOWN_LEXEMES.find { it.value == char.toString() }?.type ?: LexemeType.DELIMITER
                            )
                        }
                        currentState = State.START
                    }
                }

                State.NUMBER -> {
                    if (char.isDigit() || (char == '.' && lexemeBuffer.contains('.').not())) {
                        addToBuffer(char)
                    } else {
                        addLexeme(index = index, value = lexemeBuffer, type = LexemeType.CONSTANT)
                        if (!char.isWhitespace()) {
                            addLexeme(
                                index = index,
                                value = char.toString(),
                                type = KNOWN_LEXEMES.find { it.value == char.toString() }?.type ?: LexemeType.DELIMITER
                            )
                        }
                        currentState = State.START
                    }
                }

                State.ASSIGN -> {
                    if (char == '=') {
                        addToBuffer(char)
                        addLexeme(index, lexemeBuffer, LexemeType.ASSIGN_SIGN)
                    } else {
                        addLexeme(index, lexemeBuffer, LexemeType.DELIMITER)
                    }
                    currentState = State.START
                }

                State.COMMENT -> {
                    if (char == '}')
                        currentState = State.START
                }
            }
        }
        println("Исходный код: \n\n\n $text \n\n\n")
        resultingLexemes.forEach {
            checkForErrors(it)
            println("${it.value} - ${it.type.asString}")
        }
        errors.sortedBy { it.index }.forEach {
            val (line, column) = getLineAndColumn(text, index = it.index)
            println("Ошибка на позиции ${it.index} - ${it.value}")
//            println("Ошибка! Строка: $line, столбец: $column - ${it.value}")
        }
        // состояние финиша ниже
    }

    private fun checkForErrors(lexeme: Lexeme) {
        if (lexeme.type == LexemeType.DELIMITER && KNOWN_LEXEMES.none { it.value == lexeme.value }) {
            reportError(
                position = lexeme.position ?: 0,
                value = "Неизвестная лексема вида ${lexeme.type.asString} - ${lexeme.value}"
            )
        }
    }

    private fun getLineAndColumn(text: String, index: Int): Pair<Int, Int> {
        TODO("доработать")
        /*var totalSymbols = 0
        var line = 0
        var column = 0
        val lines = text.lines()
        lines.forEachIndexed { i, lineText ->
            totalSymbols += lineText.length
            if (index <= totalSymbols) {
                line = i
                column = lineText.length - index
            }
        }
        return line + 1 to column + 1*/
    }

    private fun clearBuffer() {
        lexemeBuffer = ""
    }

    private fun addToBuffer(char: Char) {
        lexemeBuffer += char
    }

    private fun addLexeme(index: Int, value: String, type: LexemeType) {
        resultingLexemes.add(Lexeme(index, value, type))
    }

    private fun addLexeme(lexeme: Lexeme) {
        resultingLexemes.add(lexeme)
    }

    private fun reportError(position: Int, value: String) {
        errors.add(
            AnalyzerError(
                index = position,
                value = value
            )
        )
    }

    companion object {
        val KNOWN_LEXEMES = listOf(
            Lexeme(value = "if", type = LexemeType.CONDITIONAL_OPERATOR),
            Lexeme(value = "then", type = LexemeType.CONDITIONAL_OPERATOR),
            Lexeme(value = "else", type = LexemeType.CONDITIONAL_OPERATOR),
            Lexeme(value = ";", type = LexemeType.DELIMITER),
            Lexeme(value = "<", type = LexemeType.COMPARISON_SIGN),
            Lexeme(value = ">", type = LexemeType.COMPARISON_SIGN),
            Lexeme(value = "=", type = LexemeType.COMPARISON_SIGN),
            Lexeme(value = ":=", type = LexemeType.ASSIGN_SIGN)
        )
    }
}