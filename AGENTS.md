# AGENTS.md — Guía para asistentes de IA

> Archivo de instrucciones para agentes IA que trabajen en este repositorio (`EverTranslator`). Define stack, reglas de CI/CD y convenciones agénticas.

---

## 1. Resumen del Proyecto

**EverTranslator** es una aplicación Android de traducción en pantalla (overlay/OCR).

---

## 2. CI/CD y Workflows

### Build Pipeline & Continuous Integration (REGLA GRABADA A FUEGO)

- 🤖 **Compilaciones y CI:** Se realizan a través de GitHub Actions (`.github/workflows/ci.yml`).
- 🚀 **Compilaciones de Lanzamiento (Release):** Se realizan a través de GitHub Actions (`.github/workflows/release.yml`).
- 🛠️ **Disparador Manual Obligatorio (`workflow_dispatch`):** Todos los workflows de release (`release.yml`) deben mantener habilitado `workflow_dispatch` en su bloque `on:` y `tag_name: ${{ inputs.tag_name || github.ref_name }}` en el paso de publicación. Esto garantiza resiliencia si GitHub Actions sufre un outage o pierde un evento de tag.

### Regla de validación por entorno (REGLA GRABADA A FUEGO)

Los agentes deben validar compilaciones según el host donde se ejecutan:

- 📱 **Host tipo dispositivo móvil / embebido (p. ej. Android userspace ARM64 sin KVM): NUNCA ejecutar compilaciones pesadas locales.** `./gradlew assembleRelease`, etc., sobrecargan el sistema. Toda validación de compilación se realiza vía GitHub Actions: `git commit` + `git push` a `master` dispara el CI en GitHub. El agente lee el resultado con `gh run list` / `gh run view`.
- 💻 **Equipo físico (portátil/ordenador del mantenedor):** Se puede compilar localmente con Gradle si se desea.
- 🛠️ `gh` está autenticado como el mantenedor en el host embebido; úsalo para inspeccionar runs del CI en vez de compilar localmente.
