package com.jarvis.android.core.command

import com.jarvis.android.core.calc.CalcError
import com.jarvis.android.core.calc.CalcResult
import com.jarvis.android.core.calc.Calculator
import com.jarvis.android.core.persona.Persona
import java.text.Normalizer
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Núcleo conversacional local y determinista. Sin red, sin modelo de lenguaje.
 * Nunca afirma haber hecho algo que no hizo: lo que no está implementado se declara.
 */
class CommandProcessor(private val clock: Clock = Clock.systemDefaultZone()) {

    fun process(
        rawInput: String,
        persona: Persona = Persona(),
        pending: PendingAction? = null,
    ): Reply {
        val norm = normalize(rawInput)
        val v = persona.vocative()

        if (pending == PendingAction.CONFIRM_CLEAR_HISTORY) {
            when {
                AFFIRMATIVE.containsMatchIn(norm) -> return Reply(
                    "Listo$v. Borré el historial guardado en este teléfono.",
                    ReplyKind.CONFIRMATION,
                    effect = Effect.CLEAR_HISTORY,
                )
                NEGATIVE.containsMatchIn(norm) -> return Reply(
                    "De acuerdo$v, no borré nada.",
                    ReplyKind.CONFIRMATION,
                )
            }
            // Cualquier otra cosa cancela la confirmación pendiente y se procesa normal.
        }

        if (norm.isEmpty()) return unknown(v)

        if (CLEAR_VERB.containsMatchIn(norm) && CLEAR_OBJECT.containsMatchIn(norm)) {
            return Reply(
                "Esto borra todo el historial guardado en este teléfono y no se puede deshacer. " +
                    "¿Confirmás? Respondé «sí» para borrarlo o «no» para cancelar.",
                ReplyKind.CONFIRMATION,
                pending = PendingAction.CONFIRM_CLEAR_HISTORY,
            )
        }

        calculation(norm)?.let { return it }

        if (TIME.containsMatchIn(norm)) {
            val time = LocalTime.now(clock).format(DateTimeFormatter.ofPattern("HH:mm"))
            return Reply("Son las $time.", ReplyKind.INFO)
        }
        if (DATE.containsMatchIn(norm)) {
            val date = LocalDate.now(clock).format(DATE_FORMAT)
            return Reply("Hoy es $date.", ReplyKind.INFO)
        }

        if (GREETING.containsMatchIn(norm) && norm.split(' ').size <= 5) {
            return Reply("${timeGreeting()}$v. ¿En qué te puedo ayudar?", ReplyKind.SMALL_TALK)
        }
        if (THANKS.containsMatchIn(norm)) {
            return Reply("De nada$v. Para eso estoy.", ReplyKind.SMALL_TALK)
        }
        if (IDENTITY.containsMatchIn(norm)) {
            return Reply(
                "Soy ${persona.name}, tu asistente personal. Esta es la primera versión: un núcleo " +
                    "conversacional local con reglas y comandos. No soy un modelo de lenguaje, " +
                    "no uso Internet y todavía no tengo voz. Voy creciendo por etapas.",
                ReplyKind.INFO,
            )
        }
        if (HELP.containsMatchIn(norm)) {
            return Reply(HELP_TEXT, ReplyKind.INFO)
        }
        if (HOW_ARE_YOU.containsMatchIn(norm)) {
            return Reply("Funcionando sin problemas por ahora$v. ¿Y vos?", ReplyKind.SMALL_TALK)
        }

        if (REMINDER.containsMatchIn(norm)) {
            return Reply(
                "Todavía no tengo recordatorios ni memoria de datos personales, así que no guardé nada. " +
                    "Lo único que se guarda es el historial de este chat, en este teléfono.",
                ReplyKind.LIMITATION,
            )
        }
        if (CALENDAR.containsMatchIn(norm)) {
            return Reply(
                "Todavía no estoy conectado a tu calendario, así que no puedo decirte qué tenés para hoy.",
                ReplyKind.LIMITATION,
            )
        }
        if (DEVICES.containsMatchIn(norm)) {
            return Reply(
                "Todavía no estoy conectado a luces, televisores ni otros dispositivos, " +
                    "así que no ejecuté ninguna acción. Eso está planificado para fases futuras.",
                ReplyKind.LIMITATION,
            )
        }
        if (PHONE_ACTIONS.containsMatchIn(norm)) {
            return Reply(
                "Todavía no puedo abrir aplicaciones, hacer llamadas ni enviar mensajes. " +
                    "No hice nada de eso.",
                ReplyKind.LIMITATION,
            )
        }
        if (WEB.containsMatchIn(norm)) {
            return Reply(
                "En esta versión no tengo acceso a Internet ni permiso de red, " +
                    "así que no puedo buscar ni investigar nada.",
                ReplyKind.LIMITATION,
            )
        }

        return unknown(v)
    }

    // ---------- Cálculo ----------

    private fun calculation(norm: String): Reply? {
        val hadPrefix = CALC_PREFIX.containsMatchIn(norm)
        var t = CALC_PREFIX.replace(norm, "")
        t = t.replace('×', '*').replace('÷', '/').replace('−', '-')
        t = DECIMAL_COMMA.replace(t, "\$1.\$2")
        t = DIV_WORDS.replace(t, " / ")
        t = MUL_WORDS.replace(t, " * ")
        t = ADD_WORDS.replace(t, " + ")
        t = SUB_WORDS.replace(t, " - ")
        t = X_BETWEEN_DIGITS.replace(t, " * ")
        t = t.trim().trimEnd('=').trim()

        if (t.isEmpty() || !MATH_ONLY.matches(t) || t.none { it.isDigit() }) return null
        if (!OPERATOR.containsMatchIn(t)) return null
        if (!hadPrefix && HYPHEN_DIGITS.matches(t)) return null // "555-1234" no es una resta

        val shown = pretty(t)
        return when (val result = Calculator.evaluate(t)) {
            is CalcResult.Success ->
                Reply("$shown = ${result.format().replace('.', ',')}", ReplyKind.CALCULATION)
            is CalcResult.Failure -> Reply(
                when (result.error) {
                    CalcError.DIVISION_BY_ZERO ->
                        "$shown implica dividir por cero, y ahí ni yo llego. Probá con otro divisor."
                    CalcError.TOO_LONG -> "Esa cuenta es demasiado larga. Probá con una más corta."
                    CalcError.TOO_DEEP -> "Esa cuenta tiene demasiados paréntesis anidados."
                    CalcError.EMPTY, CalcError.SYNTAX ->
                        "No pude interpretar esa operación. Probá con algo como 12*(3+4) o «245 por 18»."
                },
                ReplyKind.CALCULATION,
            )
        }
    }

    private fun pretty(expr: String): String {
        val symbols = expr.replace('*', '×').replace('/', '÷')
        val decimals = DECIMAL_DOT.replace(symbols, "\$1,\$2")
        return MULTISPACE.replace(decimals, " ").trim()
    }

    // ---------- Utilidades ----------

    private fun timeGreeting(): String {
        val hour = LocalTime.now(clock).hour
        return when {
            hour < 6 -> "Buenas noches"
            hour < 13 -> "Buen día"
            hour < 20 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }

    private fun unknown(v: String) = Reply(
        "No logré entender eso$v. Soy un núcleo local con reglas y comandos, no un modelo de " +
            "lenguaje, así que mi alcance es acotado. Escribí «ayuda» para ver qué sé hacer.",
        ReplyKind.UNKNOWN,
    )

    internal fun normalize(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        val noMarks = MARKS.replace(Normalizer.normalize(lower, Normalizer.Form.NFD), "")
        val noQuestion = QUESTION_MARKS.replace(noMarks, "")
        val noPrefix = JARVIS_PREFIX.replace(noQuestion.trim(), "")
        return SPACES.replace(noPrefix, " ").trim()
    }

    private companion object {
        val LOCALE: Locale = Locale.forLanguageTag("es-AR")
        val DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", LOCALE)

        val MARKS = Regex("""\p{M}+""")
        val QUESTION_MARKS = Regex("""[¿?¡!]""")
        val JARVIS_PREFIX = Regex("""^jarvis\b[\s,:;.\-]*""")
        val SPACES = Regex("""\s+""")
        val MULTISPACE = Regex(""" {2,}""")

        val AFFIRMATIVE = Regex("""^(si|confirmo|confirmado|dale|ok|okay|borralo|borrar|adelante|claro)\b""")
        val NEGATIVE = Regex("""^(no|cancela|cancelar|mejor no|nunca)\b""")
        val CLEAR_VERB = Regex("""\b(borra|borrar|borrame|elimina|eliminar|limpia|limpiar)\b""")
        val CLEAR_OBJECT = Regex("""\b(historial|conversacion|chat)\b""")

        val CALC_PREFIX = Regex("""^(cuanto (es|da|son|seria|vale|nos da)|calcula(me|r)?|resolve(me)?|dame el resultado de)\s*""")
        val DECIMAL_COMMA = Regex("""(\d),(\d)""")
        val DECIMAL_DOT = Regex("""(\d)\.(\d)""")
        val DIV_WORDS = Regex("""\b(dividido por|dividido entre|dividido|entre)\b""")
        val MUL_WORDS = Regex("""\b(multiplicado por|multiplicado|por|veces)\b""")
        val ADD_WORDS = Regex("""\bmas\b""")
        val SUB_WORDS = Regex("""\bmenos\b""")
        val X_BETWEEN_DIGITS = Regex("""(?<=\d)\s*x\s*(?=\d)""")
        val MATH_ONLY = Regex("""^[0-9+\-*/().\s]+$""")
        val OPERATOR = Regex("""[+\-*/()]""")
        val HYPHEN_DIGITS = Regex("""^\d+(-\d+)+$""")

        val TIME = Regex("""\b(que hora|la hora|hora es)\b""")
        val DATE = Regex("""\b(que dia|que fecha|fecha de hoy|a cuanto estamos)\b""")
        val GREETING = Regex("""^(hola|buenas|buen dia|buenos dias|buenas tardes|buenas noches|hey|ey|que tal)\b""")
        val THANKS = Regex("""\b(gracias|muchas gracias|te agradezco)\b""")
        val IDENTITY = Regex("""\b(quien (sos|eres|es jarvis)|como te llamas|cual es tu nombre|que sos|que eres)\b""")
        val HELP = Regex("""\b(ayuda|que podes hacer|que puedes hacer|que sabes hacer|comandos)\b""")
        val HOW_ARE_YOU = Regex("""\b(como estas|como andas|como va|como te va)\b""")
        val REMINDER = Regex("""\b(acordate|acuerdate|recordame|recuerdame|recordatorio|anota|anotame)\b""")
        val CALENDAR = Regex("""\b(que tenemos|que hay para hoy|mi agenda|agenda|calendario)\b""")
        val DEVICES = Regex(
            """\b(luz|luces|lampara|televisor|tele|tv|enchufe|termostato|persiana|persianas|cerradura|volumen|prende|prender|apaga|apagar|enciende|encender)\b""",
        )
        val PHONE_ACTIONS = Regex("""\b(abri|abrir|abre|llama|llamar|manda|mandale|envia|enviale|whatsapp|mensaje|alarma|temporizador|musica|reproduci)\b""")
        val WEB = Regex("""\b(clima|noticias|busca|buscar|investiga|investigar|internet|google)\b""")

        val HELP_TEXT = "Por ahora soy un núcleo local, sin Internet ni modelo de lenguaje. Puedo:\n" +
            "• Calcular: +, −, ×, ÷, paréntesis y decimales. Ej.: «245 por 18» o «12*(3+4)».\n" +
            "• Decirte la hora y la fecha.\n" +
            "• Guardar el historial en este teléfono y borrarlo cuando quieras («borrar historial»).\n\n" +
            "Todavía no puedo: hablar o escuchar, buscar en Internet, guardar recordatorios, " +
            "ni controlar apps, luces o televisores."
    }
}
