package com.jarvis.android.core.command

import com.jarvis.android.core.persona.Persona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CommandProcessorTest {

    // Miércoles 25/11/2026 14:35 UTC
    private val clock = Clock.fixed(Instant.parse("2026-11-25T14:35:00Z"), ZoneOffset.UTC)
    private val processor = CommandProcessor(clock)

    private fun reply(input: String, persona: Persona = Persona(), pending: PendingAction? = null) =
        processor.process(input, persona, pending)

    @Test fun calcula_en_lenguaje_natural() {
        val r = reply("¿Cuánto es 245 por 18?")
        assertEquals(ReplyKind.CALCULATION, r.kind)
        assertTrue(r.text, r.text.contains("4410"))
    }

    @Test fun calcula_con_x_y_con_prefijo_jarvis() {
        assertTrue(reply("245 x 18").text.contains("4410"))
        assertTrue(reply("Jarvis, ¿cuánto es 12 por (3 + 4)?").text.contains("84"))
    }

    @Test fun calcula_con_verbo_y_operadores() {
        assertTrue(reply("calculá 2 + 3 * 4").text.contains("14"))
    }

    @Test fun acepta_coma_decimal_y_palabras() {
        assertTrue(reply("10 dividido por 4").text.contains("2,5"))
        assertTrue(reply("1,5 más 2,25").text.contains("3,75"))
        assertTrue(reply("9 menos 4").text.contains("5"))
    }

    @Test fun division_por_cero_se_explica() {
        val r = reply("5 / 0")
        assertEquals(ReplyKind.CALCULATION, r.kind)
        assertTrue(r.text.contains("cero"))
    }

    @Test fun texto_con_numeros_no_es_una_cuenta() {
        assertEquals(ReplyKind.UNKNOWN, reply("tengo 5 manzanas").kind)
        assertEquals(ReplyKind.UNKNOWN, reply("555-1234").kind)
        assertEquals(ReplyKind.UNKNOWN, reply("2024").kind)
    }

    @Test fun hora_y_fecha_usan_el_reloj() {
        assertTrue(reply("¿Qué hora es?").text.contains("14:35"))
        assertTrue(reply("qué día es hoy").text.contains("2026"))
    }

    @Test fun saludo_segun_la_hora_y_tratamiento() {
        assertTrue(reply("hola").text.startsWith("Buenas tardes"))
        assertTrue(reply("hola", Persona(addressAs = "señor")).text.contains(", señor"))
    }

    @Test fun no_finge_controlar_dispositivos() {
        val r = reply("Apagá las luces del living")
        assertEquals(ReplyKind.LIMITATION, r.kind)
        assertTrue(r.text.contains("Todavía no"))
        assertNull(r.effect)
    }

    @Test fun no_finge_guardar_recordatorios() {
        val r = reply("Acordate de que mañana tengo que revisar el televisor")
        assertEquals(ReplyKind.LIMITATION, r.kind)
        assertTrue(r.text.contains("no guardé"))
    }

    @Test fun no_finge_leer_el_calendario() {
        assertEquals(ReplyKind.LIMITATION, reply("JARVIS, ¿qué tenemos para hoy?").kind)
    }

    @Test fun no_finge_buscar_en_internet() {
        assertEquals(ReplyKind.LIMITATION, reply("buscá noticias de hoy").kind)
    }

    @Test fun borrar_historial_pide_confirmacion() {
        val ask = reply("borrar historial")
        assertEquals(PendingAction.CONFIRM_CLEAR_HISTORY, ask.pending)
        assertNull(ask.effect)

        val yes = reply("sí", pending = PendingAction.CONFIRM_CLEAR_HISTORY)
        assertEquals(Effect.CLEAR_HISTORY, yes.effect)

        val no = reply("no", pending = PendingAction.CONFIRM_CLEAR_HISTORY)
        assertNull(no.effect)
        assertNull(no.pending)
    }

    @Test fun otra_cosa_cancela_la_confirmacion() {
        val r = reply("qué hora es", pending = PendingAction.CONFIRM_CLEAR_HISTORY)
        assertNull(r.effect)
        assertTrue(r.text.contains("14:35"))
    }

    @Test fun ayuda_describe_solo_lo_real() {
        val r = reply("ayuda")
        assertEquals(ReplyKind.INFO, r.kind)
        assertTrue(r.text.contains("Todavía no puedo"))
    }

    @Test fun lo_desconocido_se_declara_como_tal() {
        assertEquals(ReplyKind.UNKNOWN, reply("blah blah").kind)
        assertEquals(ReplyKind.UNKNOWN, reply("¿qué es un átomo?").kind)
    }
}
