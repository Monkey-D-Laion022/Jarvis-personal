# Estado del proyecto — Fase 1

Fecha de esta versión: 25/11/2026. Entorno donde se generó: sandbox sin Android SDK, sin Gradle, sin compilador
Kotlin y sin red. **Nada de lo que sigue fue compilado ni ejecutado en este entorno.**

| Elemento | Estado |
|---|---|
| Código Kotlin (calculadora, procesador, Room, chat, Compose) | Implementado, **no compilado** |
| Pruebas unitarias (`CalculatorTest`, `CommandProcessorTest`) | Escritas, **no ejecutadas** (resultados esperados revisados a mano) |
| Pruebas instrumentadas de Room (`ChatDaoTest`) | Escritas, **no ejecutadas** |
| `ChatViewModel` | Implementado, **sin pruebas** |
| Workflows de GitHub Actions | Escritos, YAML validado sintácticamente, **nunca corridos** |
| Estructura y sintaxis estática (llaves, XML, TOML, YAML, alias del catálogo) | Verificadas con scripts |
| Integridad del ZIP | Verificada (`unzip -t` y lista de contenido) |
| `assembleDebug` / APK | **Pendiente**: se produce por primera vez en GitHub Actions |
| Voz, IA, Internet, hogar inteligente | Diseñado, **no implementado** (fases futuras) |

La primera compilación real puede fallar por detalles que no se pueden detectar sin compilar
(un import, una API). Si pasa, el log de Actions indica la línea exacta.
