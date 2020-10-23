package fr.istic.vandv.squeezer.algorithms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CompressionAlgorithm {

    void compress(InputStream input, OutputStream output) throws IOException;

    void decompress(InputStream input, OutputStream output) throws IOException;
}
