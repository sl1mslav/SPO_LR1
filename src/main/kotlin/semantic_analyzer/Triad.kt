package org.example.semantic_analyzer

import org.example.lexical_analyzer.Lexeme

data class Triad(
    val operator: Lexeme,
    val lOperand: String,
    val rOperand: String
) {
    override fun toString() = "${operator.value} ($lOperand, $rOperand)"
}
