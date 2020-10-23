package fr.istic.vandv.squeezer.algorithms.bitmanipulation;

import java.io.IOException;
import java.io.OutputStream;

public class BitStringWriter {

    private final OutputStream output;
    private int bitsInUse = 0;
    private int buffer = 0;

    private static final int MASK = 0xFF;

    public BitStringWriter(OutputStream output) {
        this.output = output;
    }


    public void write(byte value, int bits) throws IOException {
        if(bits < 1 || bits > 8) {
            throw new IllegalArgumentException("Bits to write must be at least 1 and no larger than 8. Got: " + bits);
        }
        buffer <<= bits;
        buffer |= (Byte.toUnsignedInt(value) >> (8 - bits));
        bitsInUse += bits;
        if(bitsInUse >= 8) {
            int extra = bitsInUse - 8;
            int sliceMask = MASK << extra;
            output.write((byte)((buffer & sliceMask) >> extra));
            buffer &= ~sliceMask;
            bitsInUse = extra;
        }
    }

    public void write(byte value) throws IOException {
        write(value, 8);
    }

    public void write(byte[] values) throws IOException {
        for (byte value : values) {
            write(value);
        }
    }

    public void write(BitString value) throws IOException {

        byte[] content = value.toByteArray();
        for (int i = 0; i < content.length - 1; i++) {
            write(content[i]);
        }
        write(content[content.length - 1], value.bitsInLastByte());
    }

    public int flush() throws IOException {
        if (bitsInUse == 0) return 0;
        output.write((byte) (buffer << (8 - bitsInUse)));
        buffer = 0;
        int result = bitsInUse;
        bitsInUse = 0;
        return result;

    }
}
