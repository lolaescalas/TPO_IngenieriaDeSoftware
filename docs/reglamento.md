# Reglamento implementado

Resumen del *Reglamento del Ajedrez* que nos dio la cátedra (PDF). Para cada regla se indica la clase que la implementa.

| Regla | Implementación |
|---|---|
| Por turnos, empiezan las blancas, una jugada por turno; no se puede pasar. | State: `WhiteTurnState` / `BlackTurnState` |
| Coordenadas: columna a-h y fila 1-8, con origen abajo a la izquierda de las blancas. La casilla de la esquina derecha de cada jugador es blanca. | `Position.parse/toString`; `ConsoleBoardRenderer` (h1 y a8 claras) |
| Posición inicial: peones en la 2da fila; T, C, A en las esquinas; Dama en su color y Rey al lado. | `BoardFactory.STANDARD_FEN` |
| Ninguna pieza sale del tablero; no hay dos piezas en una casilla; no se capturan piezas propias. | `Position`, `MovementPattern`s |
| No se puede dejar al propio rey en jaque al final del turno. | `MoveGenerator.legalMoves` |
| Torre: fila o columna hasta el borde o la primera pieza. | `SlidingMovement(ORTHOGONAL)` |
| Alfil: diagonales. | `SlidingMovement(DIAGONAL)` |
| Dama: Torre + Alfil. | `CompositeMovement` |
| Rey: una casilla en cualquier dirección (no puede quedar al lado del otro rey). | `JumpMovement(KING_OFFSETS)` + filtro de jaque |
| Caballo: en "L", salta piezas. | `JumpMovement(KNIGHT_OFFSETS)` |
| Peón: avanza 1 (o 2 la primera vez); captura en diagonal hacia adelante. | `PawnMovement` |
| Captura al paso: solo en el turno inmediatamente siguiente al avance doble. | `PawnMovement` + `Board.enPassantTarget` + `MoveCommand` |
| Coronación a Caballo, Alfil, Torre o Dama, en el mismo turno. | `PawnMovement` (genera 4 jugadas), `MoveCommand`, `PieceFactory` |
| Enroque: primer movimiento de rey y torre; sin piezas en el medio; el rey no está en jaque ni pasa por (o llega a) una casilla atacada. La torre y b1/b8 sí pueden estar atacadas. | `MoveGenerator.addCastlingMoves`, `CastlingRights` |
| Jaque mate: hay jaque y no existe jugada legal. | `GameEndDetector` |
| Tablas por ahogado. | `GameEndDetector` |
| Tablas por acuerdo. | `Game.agreeDraw` (la oferta la gestiona `GameController`) |
| Tablas por triple repetición (mismas piezas y mismas jugadas posibles). | `Game` cuenta `Board.positionKey`: piezas + turno + enroques + casilla al paso |
| Tablas por material insuficiente: R vs R, R+C vs R, R+A vs R (si hay peones no aplica). | `GameEndDetector.isInsufficientMaterial` |
| Tablas por 50 jugadas de cada bando sin captura ni movimiento de peón. | `Board.halfmoveClock` (100 medias jugadas) |
| Rendición. | `Game.resign` |

**Simplificación**: en la triple repetición, la casilla al paso se incluye en la clave siempre que exista, aunque ningún peón pueda capturar. En ese caso raro la repetición tarda una jugada más en contarse.
