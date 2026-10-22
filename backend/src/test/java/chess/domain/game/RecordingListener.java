package chess.domain.game;

import chess.domain.event.GameEventListener;
import chess.domain.event.MoveEvent;
import chess.domain.model.Color;
import chess.domain.model.Move;
import chess.domain.rules.GameResult;

import java.util.ArrayList;
import java.util.List;

/** Fake de Observer para tests: anota los eventos recibidos, en orden. */
class RecordingListener implements GameEventListener {

    final List<String> events = new ArrayList<>();

    @Override
    public void onMoveMade(MoveEvent event) {
        events.add("move " + event.move());
    }

    @Override
    public void onCheck(Color colorInCheck) {
        events.add("check " + colorInCheck);
    }

    @Override
    public void onMoveUndone(Move move) {
        events.add("undo " + move);
    }

    @Override
    public void onGameOver(GameResult result) {
        events.add("over " + result.reason());
    }
}
