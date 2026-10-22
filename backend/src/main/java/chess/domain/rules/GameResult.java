package chess.domain.rules;

import chess.domain.model.Color;

import java.util.Optional;

/** Resultado final de una partida: quién ganó (si alguien) y por qué. */
public record GameResult(Color winner, Reason reason) {

    public enum Reason {
        CHECKMATE("jaque mate"),
        RESIGNATION("rendición"),
        STALEMATE("ahogado"),
        AGREEMENT("tablas por acuerdo"),
        THREEFOLD_REPETITION("triple repetición"),
        INSUFFICIENT_MATERIAL("material insuficiente"),
        FIFTY_MOVE_RULE("regla de las 50 jugadas");

        private final String description;

        Reason(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    public static GameResult win(Color winner, Reason reason) {
        return new GameResult(winner, reason);
    }

    public static GameResult draw(Reason reason) {
        return new GameResult(null, reason);
    }

    public boolean isDraw() {
        return winner == null;
    }

    public Optional<Color> getWinner() {
        return Optional.ofNullable(winner);
    }

    @Override
    public String toString() {
        String who = isDraw() ? "Tablas" : (winner == Color.WHITE ? "Ganan las blancas" : "Ganan las negras");
        return who + " por " + reason.description();
    }
}
