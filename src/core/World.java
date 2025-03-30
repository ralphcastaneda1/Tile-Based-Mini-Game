package core;


import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class World {
    private static final int LEAF_MIN = 15;
    private static final int LEAF_MAX = 30;
    private static final String SAVE_FILE = "src/save_data.txt";
    private Random random;     // random seed
    private TETile[][] world;  // 2d world of TETiles
    private BSPTree bsp;       // BSPTree that stores rooms and hallways
    private Position player;   // Position of the player
    private Position treasure; // Position of the treasure
    private List<Enemy> enemies;
    private int roomIndexPlayer, roomIndexTreasure;
    private Pathfinding pathfinder;
    private static final int MIN_DISTANCE_FROM_PLAYER = 12; // Minimum tiles away from the player

    private static final int MIN_DISTANCE_FROM_ERDTREE = 75;



    /**
     * Constructor World. Initialize World with TETile of NOTHING.
     * Randomly assign the number of potential rooms using BSPTree leaves.
     * Initialize bsp a Binary Space Partition Tree.
     * Create Players and Treasures.
     * Generate the 2D world of TETiles of connected rooms and hallways.
     * @param seed
     * @param width
     * @param height
     */
    World(long seed, int width, int height) {
        random = new Random(seed);
        world = new TETile[width][height - 3];
        initializeWorld();
        int leafNum = RandomUtils.uniform(random, LEAF_MAX - LEAF_MIN + 1) + LEAF_MIN;
        bsp = new BSPTree(width, height - 3, leafNum, random);
        createPlayerAndTreasure();
        createEnemies();
        scatterTilesBackDrop();
        generateWorld();
        pathfinder = new Pathfinding(world);
    }

    /**
     * A Initializer that add TETiles of NOTHING in world
     */
    private void initializeWorld() {
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void scatterTilesBackDrop() {
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                if (random.nextDouble() < 1) {
                    world[x][y] = Tileset.RUNE;
                }
            }
        }
    }
    /**
     * A generater that add rooms and hallways to world
     */
    private void generateWorld() {
        List<Room> rooms = bsp.rooms();
        for (Room r : rooms) {
            addRoom(r);
        }
        List<Room> hallways = bsp.hallways();
        for (Room h : hallways) {
            addRoom(h);
        }
        addTile(player, Tileset.TARNISHED);
        addTile(treasure, Tileset.ELDRITCH_TREE);
        for (Enemy enemy : enemies) {
            addTile(enemy.getPosition(), Tileset.ENEMY);
        }
        removeIsolatedWallTiles();
        removeSingleTileRooms();
        convertEnclosedFloorsToWalls();
        removeDeadEnds();
        eliminateOneTileRooms();
    }

    private void removeDeadEnds() {
        boolean changesMade;
        do {
            changesMade = false;
            for (int x = 0; x < world.length; x++) {
                for (int y = 0; y < world[0].length; y++) {
                    if (world[x][y].equals(Tileset.ASHEN_SKY) && isDeadEnd(x, y)) {
                        world[x][y] = Tileset.ELDENWALL;
                        changesMade = true;
                    }
                }
            }
        } while (changesMade);
    }

    private boolean isDeadEnd(int x, int y) {
        int walls = 0;
        if (isWall(x + 1, y)) walls++;
        if (isWall(x - 1, y)) walls++;
        if (isWall(x, y + 1)) walls++;
        if (isWall(x, y - 1)) walls++;
        return walls == 3;
    }

    private void removeIsolatedWallTiles() {
        boolean[][] visited = new boolean[world.length][world[0].length];
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                if (world[x][y].equals(Tileset.ELDENWALL) && !visited[x][y]) {
                    List<Position> connectedWalls = new ArrayList<>();
                    floodFillWallTiles(x, y, visited, connectedWalls);
                    if (isIsolatedWallBlock(connectedWalls)) {
                        for (Position wall : connectedWalls) {
                            world[wall.x][wall.y] = Tileset.RUNE;
                        }
                    }
                }
            }
        }
    }

    private void floodFillWallTiles(int x, int y, boolean[][] visited, List<Position> connectedWalls) {
        if (x < 0 || x >= world.length || y < 0 || y >= world[0].length) return;
        if (visited[x][y] || !world[x][y].equals(Tileset.ELDENWALL)) return;
        visited[x][y] = true;
        connectedWalls.add(new Position(x, y));
        floodFillWallTiles(x + 1, y, visited, connectedWalls);
        floodFillWallTiles(x - 1, y, visited, connectedWalls);
        floodFillWallTiles(x, y + 1, visited, connectedWalls);
        floodFillWallTiles(x, y - 1, visited, connectedWalls);
    }

    private boolean isIsolatedWallBlock(List<Position> wallBlock) {
        if (wallBlock.size() > 12) return false;
        for (Position wall : wallBlock) {
            if (isAdjacentToNonWall(wall.x, wall.y)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAdjacentToNonWall(int x, int y) {
        return checkTile(x + 1, y, Tileset.ASHEN_SKY) ||
                checkTile(x - 1, y, Tileset.ASHEN_SKY) ||
                checkTile(x, y + 1, Tileset.ASHEN_SKY) ||
                checkTile(x, y - 1, Tileset.ASHEN_SKY) ||
                checkTile(x + 1, y, Tileset.NOTHING) ||
                checkTile(x - 1, y, Tileset.NOTHING) ||
                checkTile(x, y + 1, Tileset.NOTHING) ||
                checkTile(x, y - 1, Tileset.NOTHING);
    }

    private boolean checkTile(int x, int y, TETile tileType) {
        if (x >= 0 && x < world.length && y >= 0 && y < world[0].length) {
            return world[x][y].equals(tileType);
        }
        return false;
    }
    /**
         * adds room to the world
         * @param room
         */
    private void addRoom(Room room) {
        if (room.width() == 1 || room.height() == 1) {
            addHallway(room);
        }
        if (room.width() <= 1 || room.height() <= 1) {
            return;
        }
       else {
            for (int y = 0; y < room.height(); y++) {
                addRowOfRoom(room, y);
            }
        }
    }

    private void removeSingleTileRooms() {
        for (int x = 1; x < world.length - 1; x++) {
            for (int y = 1; y < world[0].length - 1; y++) {
                if (isRemovableWallTile(x, y)) {
                    adjustSingleTileRoom(x, y);
                }
            }
        }
    }


    private void eliminateOneTileRooms() {
        for (int x = 1; x < world.length - 1; x++) {
            for (int y = 1; y < world[0].length - 1; y++) {
                if (world[x][y].equals(Tileset.ASHEN_SKY) && isSurroundedByWalls(x, y)) {
                    world[x][y] = Tileset.ELDENWALL;
                }
            }
        }
    }

    private boolean isSurroundedByWalls(int x, int y) {
        return isWall(x + 1, y) && isWall(x - 1, y) && isWall(x, y + 1) && isWall(x, y - 1);
    }

    private boolean isEnclosedFloor(int x, int y) {
        return isWall2(x + 1, y) && isWall2(x - 1, y) && isWall2(x, y + 1) && isWall2(x, y - 1);
    }

    private boolean isWall2(int x, int y) {
        if (!isWithinBounds(x, y)) return false;
        TETile tile = world[x][y];
        return tile.equals(Tileset.ELDENWALL);
    }

    private void convertEnclosedFloorsToWalls() {
        for (int x = 0; x < world.length; x++) {
            for (int y = 0; y < world[0].length; y++) {
                if (world[x][y].equals(Tileset.ASHEN_SKY) && isEnclosedFloor(x, y)) {
                    world[x][y] = Tileset.ELDENWALL;
                }
            }
        }
    }

    private boolean isRemovableWallTile(int x, int y) {
        return isWall(x, y) &&
                isWall(x + 1, y) && isWall(x, y + 1) &&
                !isFloor(x + 1, y + 1) &&
                isWall(x - 1, y) && isWall(x, y - 1) &&
                !isFloor(x - 1, y - 1);
    }

    private boolean isFloor(int x, int y) {
        return isWithinBounds(x, y) && world[x][y].equals(Tileset.ASHEN_SKY);
    }

    private boolean isWall(int x, int y) {
        return isWithinBounds(x, y) && world[x][y].equals(Tileset.ELDENWALL);
    }

    private void adjustSingleTileRoom(int x, int y) {
        if (isRemovableWallTile(x, y)) {
            world[x][y] = Tileset.ASHEN_SKY;
        }
    }

    private void addHallway(Room hallway) {
        for (int x = hallway.xOffset(); x < hallway.xOffset() + hallway.width(); x++) {
            for (int y = hallway.yOffset(); y < hallway.yOffset() + hallway.height(); y++) {
                if (isWithinBounds(x, y)) {
                    world[x][y] = Tileset.ASHEN_SKY;
                    connectAdjacentTiles(x, y);
                }
            }
        }
    }

    private boolean isWithinBounds(int x, int y) {
        boolean withinBounds = x >= 0 && x < world.length && y >= 0 && y < world[0].length;
        if (!withinBounds) {
            System.err.println("Attempted to place a tile outside of world bounds at: " + x + ", " + y);
        }
        return withinBounds;
    }

    private void connectAdjacentTiles(int x, int y) {
        if (x > 0 && isWithinBounds(x - 1, y) && world[x - 1][y] != Tileset.ASHEN_SKY) {
            world[x - 1][y] = Tileset.ELDENWALL;
        }
        if (x < world.length - 1 && isWithinBounds(x + 1, y) && world[x + 1][y] != Tileset.ASHEN_SKY) {
            world[x + 1][y] = Tileset.ELDENWALL;
        }
        if (y < world[0].length - 1 && isWithinBounds(x, y + 1) && world[x][y + 1] != Tileset.ASHEN_SKY) {
            world[x][y + 1] = Tileset.ELDENWALL;
        }
        if (y > 0 && isWithinBounds(x, y - 1) && world[x][y - 1] != Tileset.ASHEN_SKY) {
            world[x][y - 1] = Tileset.ELDENWALL;
        }
    }

    /**
     * adds a row of room to the world.
     * If the row is the first or the last row, add all TETiles of WALL
     * Otherwise, add TETiles of WaLL to the first and the last tile
     * and TETiles of FLOOR to the rest tiles
     * @param room
     * @param row
     */
    private void addRowOfRoom(Room room, int row) {
        int y = room.yOffset() + row;
        int xLast = room.xOffset() + room.width() - 1;
        if (row == 0 || row == room.height() - 1) {
            for (int x = room.xOffset(); x <= xLast; x++) {
                addTile(new Position(x, y), Tileset.ELDENWALL);
            }
            return;
        }
        addTile(new Position(room.xOffset(), y), Tileset.ELDENWALL);
        for (int x = room.xOffset() + 1; x < xLast; x++) {
            addTile(new Position(x, y), Tileset.ASHEN_SKY);
        }
        addTile(new Position(xLast, y), Tileset.ELDENWALL);
    }

    /**
     * Adds a tile t to the world to the given position p in the world.
     * when the current tile is FLOOR, Wall is added to prevent overlapping
     * @param p the position in the world to add Tiles on
     * @param t the TETile that we want to add
     */

    private void addTile(Position p, TETile t) {
        if (!t.equals(Tileset.ELDENWALL) || !world[p.x][p.y].equals(Tileset.ASHEN_SKY)) {
            world[p.x][p.y] = t;
        }
    }

    /**
     * add palyer and treasures to all rooms in binary space partition tree bsp
     */
    private void createPlayerAndTreasure() {
        List<Room> rooms = bsp.rooms();
        if (rooms.size() < 2) {
            System.err.println("Not enough rooms to place player and treasure separately.");
            return;
        }
        int currentMinDistance = MIN_DISTANCE_FROM_ERDTREE;
        boolean validPlacement = false;
        while (!validPlacement && currentMinDistance > 0) {
            validPlacement = attemptToPlacePlayerAndTreasure(rooms, currentMinDistance);
            if (!validPlacement) {
                currentMinDistance -= 2;
            }
        }
        if (!validPlacement) {
            System.err.println("Failed to place player and treasure even with reduced minimum distance.");
        }
    }

    private boolean attemptToPlacePlayerAndTreasure(List<Room> rooms, int minDistance) {
        Room playerRoom, treasureRoom;
        Position potentialPlayer, potentialTreasure;

        for (int attempts = 0; attempts < 100; attempts++) {
            int playerRoomIndex = RandomUtils.uniform(random, rooms.size());
            playerRoom = rooms.get(playerRoomIndex);
            potentialPlayer = new Position(playerRoom.xOffset() + playerRoom.width() / 2, playerRoom.yOffset() + playerRoom.height() / 2);

            int treasureRoomIndex = RandomUtils.uniform(random, rooms.size());
            while (treasureRoomIndex == playerRoomIndex) {
                treasureRoomIndex = RandomUtils.uniform(random, rooms.size());
            }
            treasureRoom = rooms.get(treasureRoomIndex);
            potentialTreasure = new Position(treasureRoom.xOffset() + treasureRoom.width() / 2, treasureRoom.yOffset() + treasureRoom.height() / 2);

            if (calculateDistance(potentialPlayer, potentialTreasure) >= minDistance) {
                player = potentialPlayer;
                treasure = potentialTreasure;
                addTile(player, Tileset.TARNISHED);
                addTile(treasure, Tileset.ELDRITCH_TREE);
                return true;
            }
        }

        return false;
    }

    private void createEnemies() {
        enemies = new ArrayList<>();
        int numberOfEnemies = 10;
        List<Integer> usedRoomIndices = new ArrayList<>();
        for (int i = 0; i < numberOfEnemies; i++) {
            int roomIndex;
            Room room;
            Position enemyPosition;
            do {
                roomIndex = RandomUtils.uniform(random, bsp.rooms().size());
                room = bsp.rooms().get(roomIndex);
                enemyPosition = findValidPositionInRoom(room);
            } while (usedRoomIndices.contains(roomIndex) || roomIndex == roomIndexPlayer || roomIndex == roomIndexTreasure
                    || enemyPosition == null || enemyPosition.equals(player) || isPositionOccupiedByEnemy(enemyPosition));
            usedRoomIndices.add(roomIndex);
            if (enemyPosition != null) {
                enemies.add(new Enemy(enemyPosition, Tileset.ENEMY, world));
            }
        }
    }

    private boolean isPositionOccupiedByEnemy(Position position) {
        for (Enemy enemy : enemies) {
            if (enemy.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    private Position findValidPositionInRoom(Room room) {
        Position position;
        int attempts = 50;
        do {
            int x = RandomUtils.uniform(random, room.xOffset() + 1, room.xOffset() + room.width() - 1);
            int y = RandomUtils.uniform(random, room.yOffset() + 1, room.yOffset() + room.height() - 1);
            position = new Position(x, y);
            attempts--;
        } while ((!isValidEnemyPosition(position.x, position.y) || isPositionTooCloseToPlayer(position))
                && attempts > 0);

        return attempts > 0 ? position : null;
    }

    private boolean isPositionTooCloseToPlayer(Position position) {
        return calculateDistance(position, player) < MIN_DISTANCE_FROM_PLAYER;
    }

    private int calculateDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    private boolean isValidEnemyPosition(int x, int y) {
        return x >= 0 && x < world.length && y >= 0 && y < world[0].length &&
                !world[x][y].description().equals("wall") && !world[x][y].description().equals("non-walkable");
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
    /**
     * locate player's position and save this position as target
     * save the world at position target to t a TETile
     * win if player met with treasure
     * lose if player met with previous track
     * game otherwise continues
     * @param d direction of the player in the game
     * @return game status
     */
    public Engine.Status movePlayer(Engine.Direction d) {
        Position target = target(d);
        TETile t = world[target.x][target.y];
        if (!t.equals(Tileset.ELDENWALL)) {
            addTile(player, Tileset.MISTY_FOREST);
            addTile(target, Tileset.TARNISHED);
            player = target;
            if (t.equals(Tileset.ELDRITCH_TREE)) {
                return Engine.Status.WIN;
            }
        }
        return Engine.Status.PLAY;
    }

    /**
     * return the target position based on the player's current position
     * @param d the direction of the player
     * @return the target position
     */
    private Position target(Engine.Direction d) {
        switch (d) {
            case UP: return new Position(player.x, player.y + 1);
            case RIGHT: return new Position(player.x + 1, player.y);
            case DOWN: return new Position(player.x, player.y - 1);
            case LEFT: return new Position(player.x - 1, player.y);
            default: return player;
        }
    }

    /**
     *
     * @return TETile 2d array
     */
    public TETile[][] worldFrame() {
        return world;
    }

    /**
     *
     * @return the position of the player
     */
    public Position getPlayer() {
        return player;
    }

    /**
     *
     * @return the position of the treasure
     */
    public Position getTreasure() {
        return treasure;
    }

    /**
     * Saves the state of the current state of the board into the
     * save.txt file (make sure it's saved into this specific file).
     * 0 represents NOTHING, 1 represents a CELL.
     */
    public void saveBoard() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write(TETile.toString(world));
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Position getPlayerPosition() {
        return player;
    }

    public boolean isPlayerCaptured() {
        Position playerPosition = getPlayerPosition();
        for (Enemy enemy : getEnemies()) {
            if (enemy.getPosition().equals(playerPosition)) {
                return true;
            }
        }
        return false;
    }
}
