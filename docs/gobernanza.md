# Gobernanza del TPO (Unidad 05, versión liviana)

## Políticas del equipo
| Nivel | Contenido |
|---|---|
| **Política** | Ningún cambio llega a `main` sin tests en verde y sin la revisión de otro integrante. |
| **Estándar** | Java 21. Identificadores en inglés y documentación/Javadoc en español. Cada clase que implementa un concepto de la materia lo indica en su Javadoc (ej. "Patrón State"). |
| **Procedimiento** | 1) Crear la rama `feature/<tema>`. 2) Programar con tests. 3) Correr `mvnw test`. 4) Abrir un Pull Request. 5) Revisión. 6) Merge. |
| **Guía** | Commits cortos en presente ("agrega enroque"). Preferir composición sobre herencia. Las reglas nuevas siempre van con un test FEN. |

## Registro de riesgos
Probabilidad e impacto en escala 1 (bajo) a 3 (alto); nivel = probabilidad × impacto.

| Riesgo | Prob. | Impacto | Nivel | Tratamiento |
|---|---|---|---|---|
| Error sutil en las reglas (enroque, al paso) | 2 | 3 | 6 | **Mitigar**: tests perft contra valores publicados. |
| La IA tarda demasiado | 2 | 2 | 4 | **Mitigar**: presupuesto de tiempo y test de "SLA". |
| Integrantes con distintas versiones de Java/Maven | 2 | 2 | 4 | **Mitigar**: Maven Wrapper y `release 21`. |
| Cambios de consigna de la cátedra | 2 | 2 | 4 | **Aceptar**: la arquitectura hexagonal permite cambiar la UI sin tocar el núcleo. |
| Pérdida de código | 1 | 3 | 3 | **Transferir**: repositorio remoto en GitHub. |
