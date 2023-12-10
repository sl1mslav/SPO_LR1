package org.example.lexical_analyzer

data class Lexeme(
    val value: String,
    val type: LexemeType
)

enum class LexemeType(val asString: String) {
    CONDITIONAL_OPERATOR("Оператор условия"),
    DELIMITER("Разделитель"),
    IDENTIFIER("Идентификатор"),
    COMPARISON_SIGN("Знак сравнения"),
    CONSTANT("Константа"),
    ASSIGN_SIGN("Знак присваивания")
}
