package org.example.lexical_analyzer

import java.io.StringReader

class Analyzer {

    private enum class State {
        START,
        IDENTIFIER,
        NUMBER,
        DELIMITER,
        ASSIGN,
        COMMENT,
        ERROR,
        FINISH
    }

    private var reader: StringReader? = null
    private var currentState = State.START
    private var currentChar: Char? = ' '
    private var number: Float = 0f
    private var lexemeBuffer: String = ""
    val resultingLexemes = mutableListOf<Lexeme>()

    fun analyze(text: String) = runCatching {
        analyze(text.reader())
    }

    private fun analyze(reader: StringReader) {
        this.reader = reader
        while (currentState != State.FINISH) {
            when (currentState) {
                State.START -> {
                    when {
                        currentChar in listOf(' ', '\n', '\t', '\r') -> {
                            readNextChar()
                        }

                        currentChar!!.isLetter() -> {
                            clearBuffer()
                            lexemeBuffer += currentChar
                            currentState = State.IDENTIFIER
                            readNextChar()
                        }

                        currentChar!!.isDigit() -> {
                            clearBuffer()
                            lexemeBuffer += currentChar
                            currentState = State.NUMBER
                            readNextChar()
                        }

                        currentChar == '{' -> {
                            // currentState = State.COMMENT
                            readNextChar()
                        }

                        currentChar == ':' -> {
                            clearBuffer()
                            lexemeBuffer += currentChar
                            currentState = State.ASSIGN
                            readNextChar()
                        }

                        currentChar == null -> {
                            currentState = State.FINISH
                        }

                        else -> {
                            clearBuffer()
                            lexemeBuffer += currentChar
                            currentState = State.DELIMITER
                        }
                    }
                }

                State.IDENTIFIER -> {
                    if (currentChar!!.isLetterOrDigit()) {
                        lexemeBuffer += currentChar
                        readNextChar()
                    } else {
                        resultingLexemes.add(
                            KNOWN_LEXEMES.find { it.value == lexemeBuffer + currentChar } ?: Lexeme(
                                value = lexemeBuffer,
                                type = LexemeType.IDENTIFIER
                            )
                        )
                        currentState = State.START
                    }
                }

                State.NUMBER -> {
                    if (currentChar!!.isDigit() || currentChar == '.') {
                        lexemeBuffer += currentChar
                    } else if (currentChar!!.isLetter()) {
                        // еррор
                        currentState = State.ERROR
                    } else {
                        resultingLexemes.add(
                            Lexeme(
                                value = lexemeBuffer + currentChar,
                                type = LexemeType.CONSTANT
                            )
                        )
                        currentState = State.START
                    }
                }

                State.DELIMITER -> {
                    clearBuffer()
                    lexemeBuffer += currentChar

                    KNOWN_LEXEMES.find { it.value == lexemeBuffer }?.let {
                        resultingLexemes.add(it)
                        currentState = State.START
                        readNextChar()
                    } ?: run {
                        // еррор
                        currentState = State.ERROR
                    }
                }

                State.ASSIGN -> {
                    if (currentChar == '=') {
                        lexemeBuffer += currentChar
                        resultingLexemes.add(
                            Lexeme(
                                value = lexemeBuffer,
                                type = LexemeType.ASSIGN_SIGN
                            )
                        )
                    } else {
                        resultingLexemes.add(
                            Lexeme(
                                value = lexemeBuffer,
                                type = LexemeType.DELIMITER
                            )
                        )
                    }
                    currentState = State.START
                }

                State.COMMENT -> {

                }

                State.ERROR -> {
                    println("Ошибка")
                    currentState = State.FINISH
                }

                State.FINISH -> {
                    println("Анализ окончен")
                }
            }
        }
    }

    private fun readNextChar() {
        val char = reader!!.read()
        currentChar = if (char == -1) null else Char(char)
    }

    private fun clearBuffer() {
        lexemeBuffer = ""
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