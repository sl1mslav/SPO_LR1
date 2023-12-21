package org.example.semantic_analyzer

import org.example.lexical_analyzer.Lexeme
import org.example.lexical_analyzer.LexemeType
import org.example.syntax_analyzer.Node

class SemanticAnalyzer {
    private val triads = mutableListOf<Triad>()

    fun analyzeNode(node: Node): List<Triad> {
        node.children.forEach { childNode ->
            val childLexemes = childNode.children.map { it.lexeme }
            when {
                childLexemes.any { it?.type == LexemeType.ASSIGN_SIGN } -> {
                    addAssignment(childNode)
                }

                childLexemes.any { it?.value == "else" } -> {
                    addFullConditional(childNode)
                }

                childLexemes.any { it?.type == LexemeType.CONDITIONAL_OPERATOR } -> {
                    addConditional(childNode)
                }
            }
        }
        return triads
    }

    fun reduce(triads: List<Triad>): List<Triad> {
        val newTriads = mutableListOf<Triad>()
        val idsWithConstants = mutableMapOf<String, String>()
        val conditionalTriadLinks = triads.filter { it.operator.value == "if" || it.operator.value == "jmp" }
            .map { it.rOperand.removePrefix("^").toInt() }
        val assignmentsToIgnore = conditionalTriadLinks.filter { triads[it - 1].operator.type == LexemeType.ASSIGN_SIGN }
        triads.forEachIndexed { index, triad ->
            if (triad.operator.type == LexemeType.ASSIGN_SIGN) {
                if (triad.rOperand in idsWithConstants.keys && (index + 1) !in assignmentsToIgnore) {
                    idsWithConstants[triad.lOperand] = idsWithConstants[triad.rOperand]!!
                    newTriads.add(triad.copy(rOperand = idsWithConstants[triad.rOperand]!!))
                }
                else {
                    idsWithConstants[triad.lOperand] = triad.rOperand
                    newTriads.add(triad)
                }
            } else if (triad.operator.type == LexemeType.COMPARISON_SIGN) {
                newTriads.add(triad)
            }
            else {
                newTriads.add(triad.copy(
                    lOperand = idsWithConstants[triad.lOperand] ?: triad.lOperand,
                    rOperand = idsWithConstants[triad.rOperand] ?: triad.rOperand
                ))
            }
        }
        return newTriads
    }

    fun optimize(triads: List<Triad>): List<Triad> {
        val analyzedDuplicates = triads.mapIndexed { index, triad ->
            val subList = triads.subList(0, index)
            val prevSameTriadIndex =
                subList.indexOfFirst { it.operator.value == triad.operator.value && it.lOperand == triad.lOperand && it.rOperand == triad.rOperand }
            if (prevSameTriadIndex != -1) {
                triad.copy(
                    operator = Lexeme(value = "SAME", type = LexemeType.COMPARISON_SIGN),
                    lOperand = (prevSameTriadIndex + 1).toString(),
                    rOperand = "0"
                )
            } else
                triad
        }
        return analyzedDuplicates.map {
            var triadBuilder = it.copy()
            if (it.lOperand.contains('^') && analyzedDuplicates[it.lOperand.removePrefix("^").toInt() - 1].operator.value == "SAME") {
                triadBuilder = triadBuilder.copy(lOperand = "^" + analyzedDuplicates[it.lOperand.removePrefix("^").toInt() - 1].lOperand)
            }
            if (it.rOperand.contains('^') && analyzedDuplicates[it.rOperand.removePrefix("^").toInt() - 1].operator.value == "SAME") {
                triadBuilder = triadBuilder.copy(rOperand = "^" + analyzedDuplicates[it.rOperand.removePrefix("^").toInt() - 1].lOperand)
            }
            triadBuilder
        }
    }

    private fun addAssignment(node: Node): Int {
        val children = node.children
        addTriad(
            operator = children[1].lexeme!!,
            lOperand = children.first().children.first().lexeme!!.value,
            rOperand = children.last().children.first().lexeme!!.value
        )
        return triads.lastIndex
    }

    private fun addComparison(node: Node): Int {
        val children = node.children
        addTriad(
            operator = children[1].lexeme!!,
            lOperand = children.first().children.first().lexeme!!.value,
            rOperand = children.last().children.first().lexeme!!.value
        )
        return triads.lastIndex
    }

    private fun addConditional(node: Node): Int {
        val children = node.children
        val comparisonTriadIndex = addComparison(children[1])
        val assignmentTriadIndex = addAssignment(children.last())
        addTriad(
            operator = children.first().lexeme!!,
            lOperand = "^${comparisonTriadIndex + 1}",
            rOperand = "^${assignmentTriadIndex + 1}"
        )
        return triads.lastIndex
    }

    private fun addFullConditional(node: Node): Int {
        val children = node.children
        val comparisonTriadIndex = addComparison(children[1])

        val thenBlockNode = children[3]
        val thenBlockIndex = when {
            thenBlockNode.children.map { it.lexeme }.any { it?.value == "else" } -> addFullConditional(thenBlockNode)
            thenBlockNode.children.map { it.lexeme }.any { it?.type == LexemeType.ASSIGN_SIGN } -> addAssignment(
                thenBlockNode
            )

            else -> addConditional(thenBlockNode)
        }

        val elseBlockNode = children.last()
        val elseBlockIndex = when {
            elseBlockNode.children.map { it.lexeme }.any { it?.value == "else" } -> addFullConditional(elseBlockNode)
            elseBlockNode.children.map { it.lexeme }.any { it?.type == LexemeType.ASSIGN_SIGN } -> addAssignment(
                elseBlockNode
            )

            else -> addConditional(elseBlockNode)
        }

        addTriad(
            operator = children.first().lexeme!!,
            lOperand = "^${comparisonTriadIndex + 1}",
            rOperand = "^${thenBlockIndex + 1}"
        )

        addTriad(
            operator = children.first().lexeme!!.copy(value = "jmp"),
            lOperand = "1",
            rOperand = "^${elseBlockIndex + 1}"
        )

        return triads.lastIndex
    }

    private fun addTriad(operator: Lexeme, lOperand: String, rOperand: String) {
        triads.add(Triad(operator, lOperand, rOperand))
    }
}