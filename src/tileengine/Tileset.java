package tileengine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset{
    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "main character", 0);
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "wall", 1);

    public static final TETile ELDENWALL = new TETile(' ', Color.white, Color.black,
            "Wall", "proj3/resources/images/Wall.png", 42);

    public static final TETile OUTSKIRTS = new TETile(' ', Color.white, Color.black,
            "Lyndell", "proj3/resources/images/moltentile.png", 43);

    public static final TETile ROCKTILE = new TETile(' ', Color.white, Color.black,
            "Lyndell", "proj3/resources/images/rocktile.png", 99);


    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black, "floor", 2);
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "abyss", 3);
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass", 4);
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "water", 5);
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "flower", 6);
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door", 7);
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door", 8);
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand", 9);
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain", 10);
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "tree", 11);

    public static final TETile CELL = new TETile('█', Color.white, Color.black, "cell", 12);

    public static final TETile TRACK = new TETile('·', Color.red, Color.black, "TRACK", 13);


    ///
    public static final TETile ELDRITCH_TREE = new TETile('Ψ', new Color(255, 255, 0), Color.black, "ErdTree", 14);
    public static final TETile RUNE = new TETile('ᚱ', new Color(128, 0, 128), Color.black, "Lands Between", 15);
    public static final TETile ASHEN_SKY = new TETile('☁', new Color(169, 169, 169), Color.black, "ashen sky", 16);
    public static final TETile SOUL_STEALER = new TETile('☠', new Color(255, 0, 0), Color.black, "soul stealer", 17);
    public static final TETile MISTY_FOREST = new TETile('✧', new Color(0, 128, 0), Color.black, "misty forest", 18);

    public static final TETile TARNISHED = new TETile(' ', Color.white, Color.black,
            "Tarnished", "proj3/resources/images/tar.png", 20);

    public static final TETile ENEMY = new TETile(' ', Color.white, Color.black,
            "Dragonlord Placidusax", "proj3/resources/images/drag3.png", 21);

    public static final TETile ENEMY2 = new TETile(' ', Color.white, Color.black,
            "Dragonlord Placidusax", "proj3/resources/images/enemy2.png", 22);

    public static final TETile TRAIL = new TETile(' ', Color.white, Color.black,
            "Fire Trail ", "proj3/resources/images/firetrail.png", 22);

}

