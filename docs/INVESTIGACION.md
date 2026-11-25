# Investigación inicial (acotada)

Hecha con búsqueda web el 25/11/2026. Solo se registran como verificados los datos que aparecieron en las fuentes citadas.
El resto de candidatos queda **pendiente de verificación** y no se adoptó nada en Fase 1.

## Vosk (reconocimiento de voz offline)
- **Enlace:** https://github.com/alphacep/vosk-api — biblioteca Android: `com.alphacephei:vosk-android` (Maven Central).
- **Función real:** STT offline, streaming, modelos pequeños (~50 MB según el README de un fork), incluye español.
- **Licencia:** Apache-2.0 (metadatos del repo y del POM de Maven Central).
- **Mantenimiento:** versión 0.3.75 en Maven Central; último push al repo según un agregador: 2025-05-01 (dato con fecha de sincronización 2025-05-05, puede estar desactualizado); ~525 issues abiertos.
- **Dependencias:** JNA (AAR).
- **Offline:** sí. **Costo:** gratis.
- **Riesgos/pendientes:** medir RAM, latencia y precisión en el Redmi 14C; confirmar tamaño y licencia del modelo de español; revisar issues de seguridad.
- **Capacidad de JARVIS:** entrada de voz offline (Fase 2).
- **Decisión:** **evaluar en Fase 2**, comparando con el reconocimiento integrado de Android (su privacidad y funcionamiento offline aún no se verificaron).

## Home Assistant Companion para Android
- **Enlace:** https://github.com/home-assistant/android (app oficial; proyecto de la Open Home Foundation).
- **Función real:** app para acceder y controlar una instancia de Home Assistant; **requiere un servidor Home Assistant**.
- **Licencia:** Apache-2.0 (listado de F-Droid).
- **Mantenimiento:** activo; versión `2026.11.1-minimal` listada en IzzyOnDroid con actualización del repo el 2026-11-06.
- **Permisos:** muy amplios (ubicación en segundo plano, NFC, etc.); no conviene embeber la app.
- **Decisión:** **evaluar en Fase 5**. JARVIS hablaría con la API de un servidor Home Assistant, no reutilizaría la app. Pendiente verificar requisitos de hardware del servidor (no hay computadora) y autenticación.

## Pendiente de verificar (no adoptado)
Motores locales de LLM para Android (llama.cpp, MediaPipe LLM Inference, etc.), TTS offline (Piper, TTS de Android), wake word (openWakeWord, Porcupine y sus términos),
whisper.cpp, MQTT. Para cada uno hay que registrar licencia, mantenimiento, requisitos y consumo **antes** de decidir.

## Proveedores cloud de IA
Selección **pendiente**: hay que verificar edad mínima, términos, cuotas y privacidad vigentes. No se integra ninguno en Fase 1 (Gemini API queda descartado, como indica la especificación).
