package chess.domain.command;

/**
 * Patrón Command (Unidad 01, clase 3): una acción como objeto, que se puede ejecutar y
 * deshacer. Regla para testearlo: el estado antes de {@code execute()} tiene que ser
 * igual al estado después de {@code undo()}.
 */
public interface Command {

    void execute();

    void undo();
}
