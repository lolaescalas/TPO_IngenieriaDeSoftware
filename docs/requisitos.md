# Requisitos

## Funcionales (RF)
| ID | Requisito | Dónde se verifica |
|---|---|---|
| RF01 | Tablero 8x8 con la posición inicial del reglamento; empiezan las blancas. | `BoardFactory.standard`, `GameTest.whiteStartsAndTurnsAlternate` |
| RF02 | Movimiento y captura de Torre, Alfil, Dama, Rey, Caballo y Peón según el reglamento. | `PieceMovementTest`, `PerftTest` |
| RF03 | No se puede salir del tablero, tener dos piezas en una casilla ni capturar piezas propias. | `Position`, `MovementPattern`s |
| RF04 | No se puede hacer una jugada que deje al propio rey en jaque. | `SpecialMovesTest.cannotLeaveOwnKingInCheck` |
| RF05 | Peón: avance doble inicial, captura al paso (solo en el turno siguiente) y coronación (C/A/T/D). | `SpecialMovesTest`, `MoveCommandTest` |
| RF06 | Enroque corto y largo con todas sus condiciones. | `SpecialMovesTest.castling*` |
| RF07 | Detección de jaque y jaque mate. | `GameTest.foolsMateIsCheckmate` |
| RF08 | Tablas: ahogado, acuerdo, triple repetición, material insuficiente, 50 jugadas. | `GameTest` |
| RF09 | Rendición. | `GameTest.resignationAndAgreement` |
| RF10 | Deshacer y rehacer jugadas. | `GameTest.undo*`, `GameControllerTest` |
| RF11 | Jugar humano vs humano o humano vs IA en 3 dificultades. | `ConsoleApp`, `AiStrategyTest` |
| RF12 | Consultar jugadas posibles de una pieza e historial de la partida. | `ConsolePlayer` (`movs e2`, `historial`) |

## No funcionales (RNF)
| ID | Requisito | Cómo se cumple |
|---|---|---|
| RNF01 | **Testeabilidad**: el núcleo se prueba sin UI. | Arquitectura hexagonal; 70 tests JUnit 5. |
| RNF02 | **Mantenibilidad / extensibilidad**: cambiar de UI o agregar una IA sin tocar las reglas. | Puertos `Player` / `GameEventListener`, Strategy, Factory. |
| RNF03 | **Rendimiento**: la IA difícil responde en ≤ 2 s. | Presupuesto de tiempo en `MinimaxStrategy` + test. |
| RNF04 | **Correctitud**: el generador de jugadas coincide con resultados *perft* publicados. | `PerftTest`. |
| RNF05 | **Portabilidad**: corre en cualquier SO con Java 21+, sin instalar Maven. | Maven Wrapper (`mvnw` / `mvnw.cmd`). |
| RNF06 | **Usabilidad**: entrada tolerante (`e2 e4`, `e2e4`, `O-O`, letras de coronación en español o inglés) y mensajes de error claros. | `MoveParser`, `ConsolePlayer`. |
