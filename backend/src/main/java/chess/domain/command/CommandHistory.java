package chess.domain.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Historial de comandos para deshacer/rehacer (dos pilas).
 * Ejecutar un comando nuevo invalida lo que se podía rehacer.
 */
public final class CommandHistory<C extends Command> {

    private final Deque<C> done = new ArrayDeque<>();
    private final Deque<C> undone = new ArrayDeque<>();

    public void executeAndRecord(C command) {
        command.execute();
        done.push(command);
        undone.clear();
    }

    public Optional<C> undo() {
        if (done.isEmpty()) {
            return Optional.empty();
        }
        C command = done.pop();
        command.undo();
        undone.push(command);
        return Optional.of(command);
    }

    public Optional<C> redo() {
        if (undone.isEmpty()) {
            return Optional.empty();
        }
        C command = undone.pop();
        command.execute();
        done.push(command);
        return Optional.of(command);
    }

    public boolean canUndo() {
        return !done.isEmpty();
    }

    public boolean canRedo() {
        return !undone.isEmpty();
    }

    /** Comandos ejecutados, del más viejo al más nuevo. */
    public List<C> executed() {
        return List.copyOf(done).reversed();
    }
}
