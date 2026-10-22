package chess.domain.model;

/** Clasificación de una jugada: las especiales necesitan lógica extra al ejecutarse/deshacerse. */
public enum MoveType {
    NORMAL,
    PAWN_DOUBLE_STEP,
    EN_PASSANT,
    CASTLE_KING_SIDE,
    CASTLE_QUEEN_SIDE,
    PROMOTION
}
