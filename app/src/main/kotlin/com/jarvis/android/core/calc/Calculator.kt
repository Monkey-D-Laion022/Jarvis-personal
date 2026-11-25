package com.jarvis.android.core.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

enum class CalcError { EMPTY, TOO_LONG, SYNTAX, DIVISION_BY_ZERO, TOO_DEEP }

sealed interface CalcResult {
    data class Success(val value: BigDecimal) : CalcResult {
        fun format(): String = Calculator.format(value)
    }

    data class Failure(val error: CalcError) : CalcResult
}

/**
 * Calculadora segura: +, -, *, /, decimales con punto, signos unarios y paréntesis.
 * Parser descendente recursivo propio: no evalúa código ni usa motores de scripting.
 */
object Calculator {
    const val MAX_LENGTH = 200
    const val MAX_DEPTH = 40
    private const val DISPLAY_SCALE = 10
    private val DIVISION_CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)

    fun evaluate(expression: String): CalcResult {
        val text = expression.trim()
        if (text.isEmpty()) return CalcResult.Failure(CalcError.EMPTY)
        if (text.length > MAX_LENGTH) return CalcResult.Failure(CalcError.TOO_LONG)
        return try {
            CalcResult.Success(Parser(text).parse())
        } catch (e: CalcException) {
            CalcResult.Failure(e.error)
        }
    }

    fun format(value: BigDecimal): String {
        val rounded = if (value.scale() > DISPLAY_SCALE) {
            value.setScale(DISPLAY_SCALE, RoundingMode.HALF_EVEN)
        } else {
            value
        }
        if (rounded.signum() == 0) return "0"
        return rounded.stripTrailingZeros().toPlainString()
    }

    private class CalcException(val error: CalcError) : Exception()

    private class Parser(private val s: String) {
        private var pos = 0
        private var depth = 0

        fun parse(): BigDecimal {
            val value = parseExpression()
            skipSpaces()
            if (pos != s.length) throw CalcException(CalcError.SYNTAX)
            return value
        }

        private fun parseExpression(): BigDecimal {
            var left = parseTerm()
            while (true) {
                skipSpaces()
                if (pos >= s.length) return left
                when (s[pos]) {
                    '+' -> { pos++; left = left.add(parseTerm()) }
                    '-' -> { pos++; left = left.subtract(parseTerm()) }
                    else -> return left
                }
            }
        }

        private fun parseTerm(): BigDecimal {
            var left = parseUnary()
            while (true) {
                skipSpaces()
                if (pos >= s.length) return left
                when (s[pos]) {
                    '*' -> { pos++; left = left.multiply(parseUnary()) }
                    '/' -> {
                        pos++
                        val right = parseUnary()
                        if (right.signum() == 0) throw CalcException(CalcError.DIVISION_BY_ZERO)
                        left = left.divide(right, DIVISION_CONTEXT)
                    }
                    else -> return left
                }
            }
        }

        private fun parseUnary(): BigDecimal {
            skipSpaces()
            if (pos < s.length && (s[pos] == '+' || s[pos] == '-')) {
                val negative = s[pos] == '-'
                pos++
                enter()
                val value = parseUnary()
                exit()
                return if (negative) value.negate() else value
            }
            return parsePrimary()
        }

        private fun parsePrimary(): BigDecimal {
            skipSpaces()
            if (pos >= s.length) throw CalcException(CalcError.SYNTAX)
            if (s[pos] == '(') {
                pos++
                enter()
                val value = parseExpression()
                exit()
                skipSpaces()
                if (pos >= s.length || s[pos] != ')') throw CalcException(CalcError.SYNTAX)
                pos++
                return value
            }
            return parseNumber()
        }

        private fun parseNumber(): BigDecimal {
            val start = pos
            var digits = 0
            var seenDot = false
            while (pos < s.length) {
                val c = s[pos]
                if (c in '0'..'9') {
                    digits++
                    pos++
                } else if (c == '.' && !seenDot) {
                    seenDot = true
                    pos++
                } else {
                    break
                }
            }
            if (digits == 0) throw CalcException(CalcError.SYNTAX)
            return BigDecimal(s.substring(start, pos))
        }

        private fun skipSpaces() {
            while (pos < s.length && s[pos].isWhitespace()) pos++
        }

        private fun enter() {
            depth++
            if (depth > MAX_DEPTH) throw CalcException(CalcError.TOO_DEEP)
        }

        private fun exit() {
            depth--
        }
    }
}
