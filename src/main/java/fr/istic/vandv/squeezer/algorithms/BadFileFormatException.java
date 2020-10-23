package fr.istic.vandv.squeezer.algorithms;

import java.io.IOException;

public class BadFileFormatException extends IOException {

    public BadFileFormatException(String message) {
        super("Wrong file format. " + message);
    }

}
