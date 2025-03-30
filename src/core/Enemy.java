package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;


class Enemy {
    private Position position;
    private Queue<Position> path;
    private TETile tile;
    private Random random = new Random();
    private int moveCooldown = 0;
    private final int cooldownMax = 0;
    private Position lastKnownPlayerPosition;
    private TETile[][] world;

    public Enemy(Position start, TETile tile, TETile[][] world) {
        this.position = start;
        this.tile = tile;
        this.world = world;
        this.lastKnownPlayerPosition = null;
        this.path = new LinkedList<>();
    }

    public void moveRandomly() {
        if (moveCooldown == 0) {
            List<Position> neighbors = getWalkableNeighbors();
            if (!neighbors.isEmpty()) {
                Position nextPosition = neighbors.get(random.nextInt(neighbors.size()));
                moveEnemyToPosition(nextPosition);
                moveCooldown = cooldownMax;
            }
        } else {
            moveCooldown--;
        }
    }

    public List<Position> getWalkableNeighbors() {
        List<Position> neighbors = new ArrayList<>();
        int[][] directions = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

        for (int[] dir : directions) {
            int newX = position.x + dir[0];
            int newY = position.y + dir[1];
            if (isValidPosition(newX, newY) && isWalkable(newX, newY)) {
                neighbors.add(new Position(newX, newY));
            }
        }
        return neighbors;
    }

    private boolean isWalkable(int x, int y) {
        TETile tile = world[x][y];
        return tile.getId() != Tileset.ELDENWALL.getId();
    }

    public void moveTowardsPlayer(Position playerPosition) {
        List<Position> neighbors = getWalkableNeighbors();
        Position bestMove = position;
        int bestDistance = distance(position, playerPosition);

        if (neighbors.isEmpty()) {
            moveRandomly();
            return;
        }
        for (Position neighbor : neighbors) {
            int newDistance = distance(neighbor, playerPosition);
            if (newDistance < bestDistance) {
                bestDistance = newDistance;
                bestMove = neighbor;
            }
        }
        if (!bestMove.equals(position)) {
            moveEnemyToPosition(bestMove);
        } else {
            moveRandomly();
        }
    }

    private int distance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < world.length && y >= 0 && y < world[0].length;
    }

    private void moveEnemyToPosition(Position nextPosition) {
        Position currentPosition = position;
        world[currentPosition.x][currentPosition.y] = Tileset.TRAIL;
        world[nextPosition.x][nextPosition.y] = tile;
        position = nextPosition;
    }

    public Position getPosition() {
        return position;
    }

}
