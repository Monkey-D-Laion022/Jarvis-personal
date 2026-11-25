package com.jarvis.android.core.calc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorTest {

    private fun ok(expr: String): String {
        val r = Calculator.evaluate(expr)
        assertTrue("Esperaba éxito en '$expr' pero fue $r", r is CalcResult.Success)
        return (r as CalcResult.Success).format()
    }

    private fun fail(expr: String): CalcError {
        val r = Calculator.evaluate(expr)
        assertTrue("Esperaba error en '$expr' pero fue $r", r is CalcResult.Failure)
        return (r as CalcResult.Failure).error
    }

    @Test fun suma_resta_multiplicacion_division() {
        assertEquals("5", ok("2+3"))
        assertEquals("-1", ok("2-3"))
        assertEquals("4410", ok("245*18"))
        assertEquals("2.5", ok("10/4"))
    }

    @Test fun precedencia_y_parentesis() {
        assertEquals("14", ok("2+3*4"))
        assertEquals("20", ok("(2+3)*4"))
        assertEquals("84", ok("12 * (3 + 4)"))
    }

    @Test fun decimales_exactos() {
        assertEquals("0.3", ok("0.1+0.2"))
        assertEquals("1", ok(".5+.5"))
        assertEquals("5", ok("5."))
    }

    @Test fun signos_unarios() {
        assertEquals("-2", ok("-5+3"))
        assertEquals("-6", ok("2*-3"))
        assertEquals("5", ok("- -5"))
        assertEquals("5", ok("+5"))
    }

    @Test fun division_se_redondea_para_mostrar() {
        assertEquals("0.3333333333", ok("1/3"))
        assertEquals("0.6666666667", ok("2/3"))
    }

    @Test fun cero_se_muestra_como_cero() {
        assertEquals("0", ok("5-5"))
        assertEquals("0", ok("0*7"))
    }

    @Test fun division_por_cero() {
        assertEquals(CalcError.DIVISION_BY_ZERO, fail("5/0"))
        assertEquals(CalcError.DIVISION_BY_ZERO, fail("1/(2-2)"))
    }

    @Test fun errores_de_sintaxis() {
        assertEquals(CalcError.SYNTAX, fail("2+"))
        assertEquals(CalcError.SYNTAX, fail("(2+3"))
        assertEquals(CalcError.SYNTAX, fail("2+3)"))
        assertEquals(CalcError.SYNTAX, fail("abc"))
        assertEquals(CalcError.SYNTAX, fail("2 3"))
        assertEquals(CalcError.SYNTAX, fail("1.5.2"))
        assertEquals(CalcError.SYNTAX, fail("*3"))
    }

    @Test fun vacio_y_demasiado_largo() {
        assertEquals(CalcError.EMPTY, fail("   "))
        assertEquals(CalcError.TOO_LONG, fail("1".repeat(Calculator.MAX_LENGTH + 1)))
    }

    @Test fun anidamiento_limitado() {
        val max = Calculator.MAX_DEPTH
        assertEquals("1", ok("(".repeat(max) + "1" + ")".repeat(max)))
        assertEquals(
            CalcError.TOO_DEEP,
            fail("(".repeat(max + 1) + "1" + ")".repeat(max + 1)),
        )
    }

    @Test fun no_ejecuta_nada_que_no_sea_aritmetica() {
        assertEquals(CalcError.SYNTAX, fail("Runtime.exec('ls')"))
        assertEquals(CalcError.SYNTAX, fail("2^3"))
    }
}
