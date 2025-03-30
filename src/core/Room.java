package core;

import java.awt.Rectangle;
import java.io.Serializable;


public class Room implements Serializable {
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private Position player;

    Room(int x, int y, int w, int h) {
        xOffset = x;
        yOffset = y;
        width = w;
        height = h;
        player = null;
    }

    /**
     * Check if this room intersects with another room.
     *
     * @param otherRoom the other room to check against
     * @return true if there is an intersection, false otherwise
     */
    public boolean intersects(Room otherRoom) {
        Rectangle thisRect = new Rectangle(xOffset, yOffset, width, height);
        Rectangle otherRect = new Rectangle(otherRoom.xOffset, otherRoom.yOffset, otherRoom.width, otherRoom.height);
        return thisRect.intersects(otherRect);
    }
    /**
     * Calculate the distance between the center of this room and another room.
     *
     * @param otherRoom the other room
     * @return the distance between the centers of the two rooms
     */
    public double distanceTo(Room otherRoom) {
        int centerX = xOffset + width / 2;
        int centerY = yOffset + height / 2;
        int otherCenterX = otherRoom.xOffset + otherRoom.width / 2;
        int otherCenterY = otherRoom.yOffset + otherRoom.height / 2;
        int dx = centerX - otherCenterX;
        int dy = centerY - otherCenterY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean isAdjacent(Room other) {
        // Check if rooms/hallways are adjacent or overlap slightly
        boolean horizontalAdjacency = (this.xOffset + this.width == other.xOffset) || (this.xOffset == other.xOffset + other.width);
        boolean verticalAdjacency = (this.yOffset + this.height == other.yOffset) || (this.yOffset == other.yOffset + other.height);

        return (horizontalAdjacency && (this.yOffset < other.yOffset + other.height && this.yOffset + this.height > other.yOffset)) ||
                (verticalAdjacency && (this.xOffset < other.xOffset + other.width && this.xOffset + this.width > other.xOffset));
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the center x-coordinate of the room.
     * @return The center x-coordinate.
     */
    public int centerX() {
        return xOffset + width / 2;
    }

    /**
     * Gets the center y-coordinate of the room.
     * @return The center y-coordinate.
     */
    public int centerY() {
        return yOffset + height / 2;
    }





    public int xOffset() {
        return xOffset;
    }

    public int yOffset() {
        return yOffset;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Position getPlayer() {
        return player;
    }

    public void setPlayer(int x, int y) {
        player = new Position(x, y);
    }

    @Override
    public String toString() {
        return ("start from " + "(" + xOffset + ", " + yOffset + ") "
                + "width=" + width + " height=" + height);
    }

}
