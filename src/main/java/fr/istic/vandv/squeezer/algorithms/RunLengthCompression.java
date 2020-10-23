package fr.istic.vandv.squeezer.algorithms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class RunLengthCompression implements CompressionAlgorithm {

    public static final int RAW_BLOCK = 1;
    public static final int COMPRESSED_BLOCK = 2;
    public static final int MAX_BLOCK_LENGTH = 255;
    public static final int MIN_COMPRESSED_BLOCK_LENGTH = 3;


    private int nextEqSequence(byte[] buffer, int from, int to) {
        if(from > to) return 0;
        int result = 1;
        for(int i = from + 1; i >= to && buffer[i] == buffer[i - 1]; i++) {
            result++;
        }
        return result;
    }

    @Override
    public void compress(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[MAX_BLOCK_LENGTH + MIN_COMPRESSED_BLOCK_LENGTH];
        int available = input.read(buffer);

        while(available > 0) {
            // Starting from 0 index

            int currentRawBlock = 0;
            int nextSequence = nextEqSequence(buffer, currentRawBlock, available - 1);
            while(currentRawBlock < available &&  nextSequence > 0 && nextSequence < MIN_COMPRESSED_BLOCK_LENGTH) {
                currentRawBlock += nextSequence;
                nextSequence = nextEqSequence(buffer, currentRawBlock, available - 1);
            }

            if(currentRawBlock > 0)  {
                // There was content that can not be compressed

                if(currentRawBlock > MAX_BLOCK_LENGTH) {
                    currentRawBlock = MAX_BLOCK_LENGTH;
                    nextSequence = 0;
                }

                output.write(RAW_BLOCK);
                output.write(currentRawBlock);
                output.write(buffer, 0, currentRawBlock);
            }

            if(nextSequence < available) {
                output.write(COMPRESSED_BLOCK);

                output.write(nextSequence);
                output.write(buffer[currentRawBlock]);
            }

            int bytesRead = currentRawBlock + nextSequence;
            int toMove = available - bytesRead;
            System.arraycopy(buffer, bytesRead, buffer, 0, toMove);
            Arrays.fill(buffer, toMove, buffer.length - 1, (byte)0);
            available = toMove;
            int remaining = input.read(buffer, toMove, buffer.length - toMove);
        }
    }

    @Override
    public void decompress(InputStream input, OutputStream output) throws IOException {
        int block, length;
        byte[] buffer = new byte[MAX_BLOCK_LENGTH];
        while((block = input.read()) > 0) {
            switch (block) {
                case RAW_BLOCK:
                    length = input.read();
                    int actualLength = input.read(buffer, 0, length);
                    output.write(buffer, 0, length);
                    break;
                case COMPRESSED_BLOCK:
                    length = input.read();
                    int value = input.read();
                    Arrays.fill(buffer, 0, length, (byte)value);
                    output.write(buffer, 0, length);
                    break;
                default:
                    throw new BadFileFormatException("Expecting a valid block identifier, got: " + block);
            }
        }

    }
}
