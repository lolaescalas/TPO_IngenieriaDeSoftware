package chess.domain.model;

/**
 * Derechos de enroque restantes. Inmutable: cada cambio devuelve una instancia nueva,
 * lo que hace trivial el {@code undo()} del Command (se guarda la instancia anterior).
 */
public record CastlingRights(boolean whiteKingSide, boolean whiteQueenSide,
                             boolean blackKingSide, boolean blackQueenSide) {

    public static final CastlingRights ALL = new CastlingRights(true, true, true, true);
    public static final CastlingRights NONE = new CastlingRights(false, false, false, false);

    public boolean canCastle(Color color, boolean kingSide) {
        if (color == Color.WHITE) {
            return kingSide ? whiteKingSide : whiteQueenSide;
        }
        return kingSide ? blackKingSide : blackQueenSide;
    }

    /** El rey se movió: pierde ambos enroques para el resto de la partida. */
    public CastlingRights withoutKing(Color color) {
        return color == Color.WHITE
                ? new CastlingRights(false, false, blackKingSide, blackQueenSide)
                : new CastlingRights(whiteKingSide, whiteQueenSide, false, false);
    }

    /** Se movió (o capturaron) la torre de esa esquina: se pierde solo ese enroque. */
    public CastlingRights withoutRookAt(Position corner) {
        boolean wk = whiteKingSide && !corner.equals(Position.of(7, 7));
        boolean wq = whiteQueenSide && !corner.equals(Position.of(7, 0));
        boolean bk = blackKingSide && !corner.equals(Position.of(0, 7));
        boolean bq = blackQueenSide && !corner.equals(Position.of(0, 0));
        return new CastlingRights(wk, wq, bk, bq);
    }

    /** Formato FEN ("KQkq" o "-"). */
    @Override
    public String toString() {
        String s = (whiteKingSide ? "K" : "") + (whiteQueenSide ? "Q" : "")
                + (blackKingSide ? "k" : "") + (blackQueenSide ? "q" : "");
        return s.isEmpty() ? "-" : s;
    }
}
