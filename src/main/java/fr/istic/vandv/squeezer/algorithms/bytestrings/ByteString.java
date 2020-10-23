package fr.istic.vandv.squeezer.algorithms.bytestrings;

import java.util.Arrays;

public class ByteString {

    public static final ByteString EMPTY = new ByteString();

    int[] bytes;

    private ByteString() { bytes = new int[0]; }

    public ByteString(int... bytes) {
        if(bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Input byte array can not be null or empty");
        }
        for(int i = 0; i < bytes.length; i++) {
            if(notValid(bytes[i]))
                throw  new IllegalArgumentException("Value " + bytes[i] + " at position " + i + " is not valid.");
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public ByteString add(int value) {
        if(notValid(value))
            throw new IllegalArgumentException("Value " + value + " is not a valid byte");
        ByteString result = new ByteString();
        result.bytes = Arrays.copyOf(bytes, bytes.length + 1);
        result.bytes[bytes.length] = value;
        return result;
    }

    private static boolean notValid(int value) {
        return value < 0 || value > 255;
    }

    public int at(int index) { return bytes[index]; }

    public int length() { return bytes.length; }

    public boolean empty() { return bytes.length == 0; }

    public byte[] toByteArray() {
        byte[] result = new byte[bytes.length];
        for(int i = 0; i < bytes.length; i++)
            result[i] = (byte) bytes[i];
        return result;
    }

    public static ByteString bytestr(int... bytes) {
        if(bytes == null || bytes.length == 0)
            return EMPTY;
        return new ByteString(bytes);
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }
}
