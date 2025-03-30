package core;

/**
 * Represents the x and y value of a tile on the 2D world.
 **/
public class Position{
    int x;
    int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    //public int hashCode() {
        //return Objects.hash(x, y);
}

