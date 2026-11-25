# JARVIS-Android — Fase 1

Asistente personal Android **local-first**, construido por etapas. Esta es la **Fase 1**: un núcleo
conversacional local, honesto y estable. No es un modelo de lenguaje ni pretende serlo todavía.

> Estado real de cada cosa (probado / no probado): ver [`docs/ESTADO.md`](docs/ESTADO.md).

## Qué hace la Fase 1

- Pantalla principal con chat (envío, historial, estados de carga y errores).
- Personalidad original y configurable (cómo querés que te llame, en *Ajustes*).
- Calculadora segura: `+ − × ÷`, decimales, signos y paréntesis. Entiende «245 por 18», «10 dividido por 4», «1,5 más 2,25».
- Hora y fecha locales.
- Historial persistente con Room, con opción de borrarlo (menú ⋮ o «borrar historial», con confirmación).
- Respuestas honestas: si pedís algo que no existe todavía (luces, calendario, recordatorios, Internet, apps), te dice que no lo hizo.
- **Sin permisos**: no pide Internet ni micrófono.

## Qué NO hace todavía

Voz, Internet, modelos de IA, recordatorios, calendario, control de dispositivos, wake word, conexión entre dispositivos.
Eso está en la hoja de ruta (abajo) y no se simula.

## Conseguir el APK solo con el celular (Redmi 14C)

**La web de GitHub NO descomprime ZIPs**, así que no sirve subir el ZIP y esperar que aparezca el proyecto.
Por eso hay dos caminos:

### Camino A (recomendado): subir solo el ZIP + un workflow chico

1. En GitHub (navegador) creá un repositorio nuevo (público o privado, es gratis y no pide tarjeta).
2. **Add file → Upload files** → elegí `JARVIS-Android-Fase1.zip` → **Commit changes**.
3. **Add file → Create new file**. En el nombre escribí exactamente `.github/workflows/build-from-zip.yml`
   y pegá el contenido del archivo `build-from-zip.yml` (se entrega junto al ZIP; también está en `bootstrap/` dentro del ZIP). **Commit changes**.
4. Pestaña **Actions** → **Build from ZIP**. Arranca sola con ese commit; si no, tocá **Run workflow**.
5. Cuando termine en verde, entrá a la ejecución → sección **Artifacts** → descargá `jarvis-debug-apk`
   (es un ZIP con el `.apk` adentro). Extraelo con la app Archivos e instalá el APK
   (Android te va a pedir permitir «instalar apps desconocidas» para esa app).
6. Si termina en rojo, abrí el paso que falló, copiá el error y pegámelo: lo corrijo.

### Camino B: repositorio con los archivos extraídos

Extraé el ZIP con la app Archivos y subí el contenido con **Upload files**. El selector del navegador móvil
suele no permitir carpetas completas, por eso este camino es incómodo. Si lo lográs, el workflow
`.github/workflows/android.yml` ya está incluido y corre solo (pruebas unitarias, APK debug y pruebas instrumentadas de Room).

## Estructura

```
app/src/main/kotlin/com/jarvis/android/
  core/calc       Calculadora segura (parser propio)
  core/command    Procesador de comandos determinista (CommandProcessor)
  core/persona    Personalidad configurable
  core/ai         Interfaz AiEngine (sin motor en Fase 1)
  core/data       Room: entidad, DAO, base de datos, repositorio
  core/settings   Preferencias no sensibles
  core/ui         Tema
  feature/chat    Pantalla, ViewModel y estado del chat
app/src/test            Pruebas unitarias (calculadora y procesador)
app/src/androidTest     Pruebas instrumentadas de Room
docs/                   Estado, decisiones técnicas e investigación
```

## Versiones fijadas

AGP 8.2.2 · Gradle 8.5 (instalado por CI, ver decisiones) · Kotlin 1.9.22 · Compose Compiler 1.5.8 ·
Compose BOM 2024.02.00 · Room 2.6.1 · KSP 1.9.22-1.0.17 · JDK 17 · minSdk 26 · compileSdk/targetSdk 34.
Todas en `gradle/libs.versions.toml`.

## Hoja de ruta

1. **Fase 1** — Núcleo Android estable (esto).
2. **Fase 2** — Voz en el teléfono (STT/TTS, activación explícita; wake word solo si Android lo permite).
3. **Fase 3** — Inteligencia conversacional (modelos locales medidos en el Redmi 14C; proveedores solo si sus términos permiten tu edad y uso).
4. **Fase 4** — Conexión entre dispositivos (emparejamiento, cifrado, Android TV).
5. **Fase 5** — Hogar inteligente.
6. **Fase 6** — Agente avanzado.

No se construye una fase sin validar la anterior.

## Licencia

Todavía no elegiste licencia para tu código; sin una, por defecto es «todos los derechos reservados».
Componentes de terceros: ver [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).
