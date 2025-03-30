package core;

/**
 * Contains the two methods that allow interacting with the inputs.
 * With reference to the example interface in InputDemo.
 */
public interface InputSource {
    public char getNextKey();
    public boolean possibleNextInput();
}
