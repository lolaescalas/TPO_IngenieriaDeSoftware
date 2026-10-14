package backend.model;

import java.util.Objects;

public record Move(Position from, Position to) {

    public Move {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Origin and destination must be different");
        }
    }

    @Override
    public String toString() {
        return from + "->" + to;
    }
}