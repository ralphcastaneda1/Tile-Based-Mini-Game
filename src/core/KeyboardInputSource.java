package core;

import edu.princeton.cs.algs4.StdDraw;

/**
 * Allows interacting with the keyboard inputs.
 * With reference to the example class in InputDemo.
 *
 **/
public class KeyboardInputSource implements InputSource {
    @Override
    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return Character.toUpperCase(StdDraw.nextKeyTyped());
            }
        }
    }

    @Override
    public boolean possibleNextInput() {
        return true;
    }
}
