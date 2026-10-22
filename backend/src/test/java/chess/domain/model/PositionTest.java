package chess.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionTest {

    @Test
    void parseAndToStringAreInverse() {
        // Arrange
        Position e2 = Position.parse("e2");
        // Act + Assert
        assertEquals(6, e2.row());
        assertEquals(4, e2.column());
        assertEquals("e2", e2.toString());
        assertEquals("a8", Position.of(0, 0).toString());
        assertEquals("h1", Position.of(7, 7).toString());
    }

    @Test
    void ofReturnsCachedInstance() {
        assertSame(Position.of(3, 3), Position.of(3, 3));
        assertSame(Position.parse("d5"), Position.of(3, 3));
    }

    @Test
    void rejectsSquaresOutsideTheBoard() {
        assertThrows(IllegalArgumentException.class, () -> Position.of(8, 0));
        assertThrows(IllegalArgumentException.class, () -> Position.parse("i1"));
        assertThrows(IllegalArgumentException.class, () -> Position.parse("a9"));
    }

    @Test
    void moveRejectsSameOriginAndDestination() {
        Position p = Position.parse("e2");
        assertThrows(IllegalArgumentException.class, () -> new Move(p, p));
    }
}
