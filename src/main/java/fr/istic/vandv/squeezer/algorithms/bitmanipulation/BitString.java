package fr.istic.vandv.squeezer.algorithms.bitmanipulation;

import java.util.Arrays;
import java.util.Objects;

public class BitString {

    private final byte[] content;
    private final int bitsOccupied;

    public BitString() {
        content = new byte[0];
        bitsOccupied = 0;
    }

    public static int requiredLenghtForBits(int bits) {
        return bits / 8 + (bits % 8 == 0? 0 : 1);
    }

    public static BitString one() { return new BitString().appendOne(); }

    public static BitString zero() { return new BitString().appendZero(); }

    public BitString(byte[] value, int bits) {
        Objects.requireNonNull(value, "Given byte array must not be null.");
        if(bits < 0) {
            throw new IllegalArgumentException("Number of bits must not be negative.");
        }
        int requiredLength = requiredLenghtForBits(bits) ;
        if(requiredLength > value.length) {
            throw new IllegalArgumentException("Number of bits must not surpass the number of bits in the given array.");
        }

        content = Arrays.copyOf(value, requiredLength);
        bitsOccupied = bits;
        //Normalization, remaining bits must be 0
        content[content.length - 1] &= (byte)(255 << freeBits());
    }

    public BitString appendZero() {
        return new BitString(Arrays.copyOf(content, content.length + (freeBits() == 0?1:0)), bitsOccupied + 1);
    }

    public BitString appendOne() {
        BitString result = appendZero();
        result.content[result.content.length - 1] |= (byte)( 1 << result.freeBits());
        return result;
    }

    public int length() { return bitsOccupied; }

    private int freeBits() { return 8*content.length - bitsOccupied; }

    public int bitsInLastByte() {
        int remainder = bitsOccupied % 8;
        if(remainder == 0) {
            return bitsOccupied == 0? 0: 8;
        }
        return remainder;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitString bitString = (BitString) o;
        return bitsOccupied == bitString.bitsOccupied &&
                Arrays.equals(content, bitString.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(bitsOccupied);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(bitsOccupied);
        for(int i = bitsOccupied - 1; i >= 0; i--) {
            builder.append( (char)(48 + (((content[i/8] >> (7 - i%8)) & 0xFF) % 2)));
        }
        return builder.reverse().toString();
    }

    public static BitString parse(String value) {
        BitString result = new BitString();
        for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '0': result = result.appendZero(); break;
                case '1': result = result.appendOne(); break;
                default: throw new IllegalArgumentException("Bit strings must contain ony 0's and 1's. Got " + c + " at index " + i + ".");
            }
        }
        return result;
    }
}
