package core;

import tileengine.TETile;

import java.io.Serializable;

// need to implement A*

public class Pathfinding implements Serializable {
    private TETile[][] tiles;
    public Pathfinding(TETile[][] tiles) {
        this.tiles = tiles;
    }
    private static class Node {
        Position position;
        Node parent;
        int g, f;

        Node(Position position) {
            this.position = position;
            this.g = Integer.MAX_VALUE;
            this.f = Integer.MAX_VALUE;
        }

        Node(Position position, Node parent, int g, int h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.f = g + h;
        }
    }
}
