package core;

import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import java.awt.*;
import java.io.*;
import java.io.IOException;

public class Engine {
    public enum Direction {
        UP, RIGHT, DOWN, LEFT
    };

    public enum Status {
        START, SEED, PLAY, WIN, LOSE
    };

    private static final int WIDTH = 60;
    private static final int HEIGHT = 43;
    private static final int COUNTDOWN = 4;
    private long lastEnemyMoveTime = 0;
    private final long enemyMoveInterval = 50; //lower # for faster enemy
    private boolean lineOfSightEnabled = false;
    private final Font TITLE_FONT = new Font("Monaco", Font.BOLD, 30);
    private final Font SUBTITLE_FONT = new Font("Monaco", Font.BOLD, 20);
    private final Font REGULAR_FONT = new Font("Monaco", Font.BOLD, 16);
    private boolean colonPressed = false;
    private Thread audioThread;
    private Sound soundManager;
    private TERenderer ter;
    private StringBuilder inputs;
    private Status status;
    private StringBuilder seedToBe;
    private long seed;
    private World world;

    public Engine() {
        ter = new TERenderer();
        soundManager = new Sound();
    }

    /**
     * Method used for exploring a fresh world. This method handles all inputs.
     */
    public void interactWithKeyboard() {
        // Initialize StdDraw
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        ter.initialize(WIDTH, HEIGHT, 0, 2);
        // Restart to the main menu when a game ends.
        // Only exit program (directly) when the users enter "Q".
        while (true) {
            initialize();
            InputSource inputSource = new KeyboardInputSource();
            startAudio("proj3/resources/audio/mainmenu.wav");
            // Handles inputs from the main menu and the prompt menu
            drawMenu();
            while (status != Status.PLAY) {
                parseMenuChoice(inputSource, true);
            }
            startAudio("proj3/resources/audio/bossfightmusic.wav");
            // Handles inputs from the game
            while (status == Status.PLAY) {
                if (StdDraw.hasNextKeyTyped()) {
                    parseMovement(inputSource, true);
                }
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastEnemyMoveTime > enemyMoveInterval) {
                    Position playerPosition = world.getPlayerPosition();
                    for (Enemy enemy : world.getEnemies()) {
                        if (enemy != null) {
                            enemy.moveTowardsPlayer(playerPosition);
                        }
                    }
                    lastEnemyMoveTime = currentTime;
                    if (world.isPlayerCaptured()) {
                        status = Status.LOSE;
                        stopAudio();
                        Thread deathSoundThread = new Thread(() -> {
                            soundManager.playWav("proj3/resources/audio/deathsound.wav");
                        });
                        deathSoundThread.start();
                        drawResult();
                        StdDraw.show();
                        try {
                            deathSoundThread.join();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Interrupted while waiting for death sound to finish: " + e.getMessage());
                        }
                        initialize();
                        status = Status.START;
                        break;
                    }
                }
                drawWorld();
                //StdDraw.show();
                StdDraw.pause(50); // do not change to keep music transition smooth
                if (status == Status.WIN) {
                    stopAudio();
                    drawResult();
                    soundManager.playWav("proj3/resources/audio/endingsound.wav");
                    break;
                }
            }
            //Let users press any key to continue
            //inputSource.getNextKey(); // had to comment this out in order to allow the auto transition from end game to the main menu
        }
    }

    private void startAudio(String filePath) {
        stopAudio();
        audioThread = new Thread(() -> {
            soundManager.playWav(filePath);
        });
        audioThread.start();
    }

    private void stopAudio() {
        if (soundManager != null) {
            soundManager.stopPlayback();
        }
        if (audioThread != null) {
            try {
                audioThread.join(500); //keep at 500 for smooth death transition to new game
            } catch (InterruptedException e) {
                System.err.println("Interrupted while stopping audio thread: " + e.getMessage());
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        initialize();
        InputSource inputSource = new StringInputSource(input);
        while (status != Status.PLAY && inputSource.possibleNextInput()) {
            parseMenuChoice(inputSource, false);
        }
        while (status == Status.PLAY && inputSource.possibleNextInput()) {
            parseMovement(inputSource, false);
        }
        if (world != null) {
            return world.worldFrame();
        }
        return null;
    }

    /**
     * Handles inputs from the main menu and the prompt menu.
     *
     * @param inputSource the inputSource
     * @param draw if it's needed to draw
     */
    private void parseMenuChoice(InputSource inputSource, boolean draw) {
        char ch = Character.toUpperCase(inputSource.getNextKey());
        // Loads the game from file
        // Prompts the users to enter the seed
        // Displays the seedToBe the users has entered
        // Initializes the world with the seed
        // Exit program
        if (status == Status.START && ch == 'L') {
            load();
        } else if (status == Status.START && ch == 'N') {
            status = Status.SEED;
            inputs.append(ch);
            if (draw) {
                drawPrompt();
            }
        } else if (status == Status.SEED && Character.isDigit(ch)) {
            seedToBe.append(ch);
            inputs.append(ch);
            if (draw) {
                drawPrompt();
            }
        } else if (status == Status.SEED && ch == 'S') {
            if (seedToBe.length() > 18) {
                seed = Long.parseLong(seedToBe.substring(0, 18));
            } else {
                seed = Long.parseLong(seedToBe.toString());
            }
            status = Status.PLAY;
            inputs.append(ch);
            world = new World(seed, WIDTH, HEIGHT);
        } else if (ch == 'Q') {
            System.exit(0);
        } /**else if (version == Version.VERSION2 && ch == 'V') {

        }**/
    }

    private void toggleLineOfSight() {
        lineOfSightEnabled = !lineOfSightEnabled;
    }

    /**
     * Handles inputs from the game.
     *
     * @param inputSource the inputSource
     * @param draw if it's needed to draw
     */
    private void parseMovement(InputSource inputSource, boolean draw) {
        char ch = Character.toUpperCase(inputSource.getNextKey());
        if (colonPressed && ch == 'Q') {
            System.exit(0);
            return;
        }
        switch (ch) {
            case 'A':
                status = world.movePlayer(Direction.LEFT);
                inputs.append(ch);
                colonPressed = false;
                break;
            case 'W':
                status = world.movePlayer(Direction.UP);
                inputs.append(ch);
                colonPressed = false;
                break;
            case 'D':
                status = world.movePlayer(Direction.RIGHT);
                inputs.append(ch);
                colonPressed = false;
                break;
            case 'S':
                status = world.movePlayer(Direction.DOWN);
                inputs.append(ch);
                colonPressed = false;
                break;
            case ':':
                save();
                colonPressed = true;
                break;
            case 'L':
                load();
                colonPressed = false;
                break;
            case 'T':
                toggleLineOfSight();
                if (lineOfSightEnabled) {
                    StdDraw.textRight(WIDTH - 1, HEIGHT - 2, "\"T\":Toggle View ON");
                }
                colonPressed = false;
                break;
            default:
                colonPressed = false;
                break;
        }
        if (draw) {
            drawWorld();
            if (status == Status.WIN || status == Status.LOSE) {
                drawResult();
            }
        }
    }

    /**
     * Initializes all member variables and resets the game.
     */
    private void initialize() {
        inputs = new StringBuilder("");
        status = Status.START;
        seedToBe = new StringBuilder("");
        seed = -1;
        world = null;
    }


    /**
     * Draws the main menu.
     */
    private void drawMenu() {
        StdDraw.clear(Color.black);
        String imagePath = "proj3/resources/images/eldenback.jpeg";
        double imageWidth = 60;
        double imageHeight = 60;
        StdDraw.picture(WIDTH / 2.0, HEIGHT - 20, imagePath, imageWidth, imageHeight);
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        StdDraw.setPenColor(Color.black);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(midWidth, midHeight + 5, "PATH TO ERDTREE");
        StdDraw.setFont(SUBTITLE_FONT);
        StdDraw.text(midWidth, midHeight - 5, "NEW JOURNEY (N)");
        StdDraw.text(midWidth, midHeight - 7, "LOAD GAME (L)");
        StdDraw.text(midWidth, midHeight - 9, "QUIT (Q)");
        StdDraw.show();
    }

    /**
     * Draws the prompt menu that lets the users enter the seed.
     */
    private void drawPrompt() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        String input = inputs.toString();
        input = input.substring(input.indexOf('N') + 1);
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        StdDraw.setFont(TITLE_FONT);
        StdDraw.text(midWidth, HEIGHT - 10, "NEW GAME");
        StdDraw.setFont(SUBTITLE_FONT);
        StdDraw.text(midWidth, midHeight, "Enter any number and then press \"S\".");
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(midWidth, midHeight - 2, input);
        StdDraw.show();
    }

    /**
     * Draws the game with {@code COUNTDOWN} seconds of full map view.
     */
    private void drawNewWorld() {
        TETile[][] worldFrame = world.worldFrame();
        for (int i = COUNTDOWN; i > 0; i--) {
            ter.renderFrame(worldFrame);
            addInstruction(worldFrame);
            //addCountdown(i);
            //StdDraw.show();
            StdDraw.pause(0);
        }
        drawWorld();
    }

    /**
     * Draws the game with a cross view.
     */
    private void drawWorld() {
        TETile[][] worldFrame = world.worldFrame();
        if (lineOfSightEnabled) {
            ter.renderTiles(world);
        } else {
            ter.renderFrame(worldFrame);
            //ter.renderRestrictedFrame(worldFrame, world.getPlayer(), world.getTreasure());
        }
        //ter.renderRestrictedFrame(worldFrame, world.getPlayer(), world.getTreasure());
        addInstruction(worldFrame);
        StdDraw.show();
    }

    /**
     * Adds instructions to StdDraw.
     */
    private void addInstruction(TETile[][] worldFrame) {
        StdDraw.setFont(REGULAR_FONT);
        StdDraw.setPenColor(Color.white);
        StdDraw.textLeft(0, HEIGHT - 0.7, "A:LEFT W:UP D:RIGHT S:DOWN");
        StdDraw.textRight(WIDTH - 1, HEIGHT - 0.7, "\":\":SAVE Q:QUIT");
        StdDraw.textRight(WIDTH - 12, HEIGHT - 0.7, "\"T\":Toggle View");
        StdDraw.setPenColor(Color.YELLOW);
        //StdDraw.text(WIDTH - 30, HEIGHT - 0.7 , "REACH THE ERDTREE");
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        int newMouseY = mouseY - 2;
        if (mouseX >= 0 && mouseX < WIDTH && newMouseY >= 0 && newMouseY < HEIGHT - 3) {
            TETile tile = worldFrame[mouseX][newMouseY];
            String tileDescription = tile.description();
            StdDraw.setFont(REGULAR_FONT);
            StdDraw.setPenColor(Color.BLACK);
            StdDraw.filledRectangle(WIDTH, HEIGHT, 0, 1);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(WIDTH - 35, HEIGHT - 0.7, "Tile: " + tileDescription);
        }
    }

    /**
     * Adds countdown messages to StdDraw.
     *
     * @param countDown the countdown in seconds
     */
    private void addCountdown(int countDown) {
        StdDraw.setFont(REGULAR_FONT);
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(WIDTH / 2, HEIGHT - 2, "Hide full map in " + countDown + " seconds...");
    }

    /**
     * Draws the game result with a full map view.
     */
    private void drawResult() {
        TETile[][] worldFrame = world.worldFrame();
        ter.renderFrame(worldFrame);
        addInstruction(worldFrame);
        StdDraw.setFont(REGULAR_FONT);
        if (status == Status.WIN) {
            StdDraw.setPenColor(Color.YELLOW);
            StdDraw.text(WIDTH / 2, HEIGHT - 2.5, "YOU'VE REACHED THE ERDTREE");
        } else if (status == Status.LOSE) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2.0, HEIGHT / 2.0, "YOU DIED");
            //StdDraw.show();
            //waitForInputToRestart();
        }
        //StdDraw.setPenColor(Color.red);
        //StdDraw.text(WIDTH / 2, 1, "PRESS ANY KEY TO BEGIN NEW JOURNEY");
        StdDraw.show();
    }

    /**
     * Saves the current inputs to a text file.
     */
    private void save() {
        File f = new File("./save_data.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(inputs.toString());
            os.writeBoolean(lineOfSightEnabled);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    /**
     * Reads the saved text file and handles the inputs in the file.
     */
    private void load() {
        File f = new File("./save_data.txt");
        String loadInputs = null;
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                loadInputs = os.readObject().toString();
                lineOfSightEnabled = os.readBoolean();
                os.close();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        interactWithInputString(loadInputs);
    }
}
