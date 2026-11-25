# Registro de decisiones técnicas

1. **Sin Gradle Wrapper.** El `gradle-wrapper.jar` es un binario que no se pudo generar sin red ni Gradle. CI instala Gradle 8.5
   con `gradle/actions/setup-gradle` y ejecuta `gradle`. Para Android Studio en una PC futura: `gradle wrapper --gradle-version 8.5`.
2. **Versiones:** AGP 8.2.2 + Gradle 8.5 + Kotlin 1.9.22 + Compose Compiler 1.5.8 + KSP 1.9.22-1.0.17 + JDK 17. Combinación pensada para
   ser compatible; **no verificada compilando** (ver ESTADO).
3. **Room con KSP** (no kapt): es el procesador recomendado y más rápido. `exportSchema = false` para no depender de un directorio de esquemas en Fase 1.
4. **Sin Hilt:** inyección manual con `AppContainer` + `ViewModelProvider.Factory`.
5. **Calculadora propia** (parser descendente recursivo con `BigDecimal`): sin evaluar código ni motores de scripting; límites de longitud (200) y anidamiento (40).
6. **Procesador determinista primero, `AiEngine` después:** las órdenes conocidas nunca pasan por un modelo; solo lo desconocido se ofrece a `AiEngine`
   (en Fase 1 es `UnavailableAiEngine`, que siempre declina). Cambiar de motor no obliga a reescribir el resto.
7. **Confirmación para acciones destructivas:** borrar el historial por chat exige «sí»; por menú, un diálogo.
8. **Sin permisos** de Internet ni micrófono, `allowBackup=false`, sin secretos en el repo ni en el APK.
9. **Preferencias** (cómo llamar al usuario) en `SharedPreferences` privadas: no son secretos. No se usa `EncryptedSharedPreferences`.
10. **Tema oscuro único** (estética HUD, menor consumo en pantallas OLED); sin modo claro en Fase 1.
11. **Textos de UI en Kotlin** y no en `strings.xml` (solo español rioplatense en Fase 1). Se externalizarán si se agrega otro idioma.
12. **Entrega vía ZIP + workflow de arranque**, porque GitHub web no descomprime ZIPs (ver README).
13. **Pruebas instrumentadas en CI** con emulador (`android-emulator-runner`); es un job separado para que un fallo del emulador no bloquee el APK.
