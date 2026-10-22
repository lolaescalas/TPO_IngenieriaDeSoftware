# TPO Ingeniería de Software — Ajedrez en Java

Ajedrez completo por consola (humano vs humano o humano vs IA), con todas las reglas del reglamento de la cátedra. Está diseñado para aplicar lo visto en la materia: arquitectura hexagonal, SOLID, patrones State, Command, Strategy, Observer, Factory y Facade, testing y optimización.

## Requisitos
- Java 21 o superior (`java -version`).
- No hace falta instalar Maven: el *wrapper* lo descarga la primera vez.

## Cómo correrlo
```bash
cd backend
./mvnw test                      # Windows: mvnw.cmd test
./mvnw package -DskipTests       # genera target/chess.jar
java -jar target/chess.jar
```

## Cómo se juega
| Comando | Acción |
|---|---|
| `e2 e4` o `e2e4` | Mover |
| `e7 e8 Q` / `e7e8=D` | Coronar (Q/R/B/N o D/T/A/C; si no se indica, Dama) |
| `O-O` / `O-O-O` | Enroque corto / largo |
| `movs e2` | Ver los destinos posibles de la pieza en e2 |
| `tablero`, `historial` | Mostrar el tablero / las jugadas realizadas |
| `deshacer`, `rehacer` | Deshacer / rehacer (contra la IA, de a dos jugadas) |
| `tablas`, `rendirse`, `salir` | Ofrecer tablas / abandonar / cerrar |

En el tablero, las mayúsculas son blancas y las minúsculas negras (K rey, Q dama, R torre, B alfil, N caballo, P peón).

## Estructura
```
backend/src/main/java/chess/
  domain/        Núcleo puro: model, movement (Strategy), command (Command),
                 rules, game (State), event (Observer), factory (Factory)
  application/   ChessFacade (Facade), GameController, MoveParser, port/Player
  adapters/      console/ (UI de texto), ai/ (IA: Random, Greedy, Minimax)
  Main.java      Composition root
docs/            arquitectura.md · requisitos.md · reglamento.md · gobernanza.md
```

Para ver qué concepto de la materia implementa cada clase, leer **[docs/arquitectura.md](docs/arquitectura.md)**.
