package fr.istic.vandv.squeezer.algorithms;

import fr.istic.vandv.squeezer.algorithms.bytestrings.ByteString;
import fr.istic.vandv.squeezer.algorithms.bytestrings.TrieDictionary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static fr.istic.vandv.squeezer.algorithms.bytestrings.ByteString.EMPTY;
import static fr.istic.vandv.squeezer.algorithms.bytestrings.ByteString.bytestr;

public class LZWCompression implements CompressionAlgorithm {

    @Override
    public void compress(InputStream input, OutputStream output) throws IOException {
        int next = input.read();
        if(next < 0) return;

        ByteString pattern = bytestr(next);

        TrieDictionary dictionary = new TrieDictionary();
        while((next = input.read()) >= 0) {
            ByteString incoming = pattern.add(next);
            if (dictionary.contains(incoming)) {
                pattern = incoming;
            } else {
                int code = dictionary.indexOf(pattern);
                writeCode(code, output);
                dictionary.add(incoming);
                pattern = bytestr(next);
            }
        }
        int code = dictionary.indexOf(pattern);
    }

    private void writeCode(int value, OutputStream output) throws IOException {
        while(value >= 255) {
            output.write(255);
            value -= 255;
        }
        output.write(value);
    }

    private int readCode(InputStream input) throws IOException {
        int acc = 0;
        int current = 0;
        while((current = input.read()) == 255) {
            acc += 255;
        }
        if(current < 0) return -1;
        return acc + current;
    }

    private List<ByteString> intializeTable() {
        ArrayList<ByteString> table = new ArrayList<>();
        for(int i = 0; i < 256; i++) {
            table.add(bytestr(i));
        }
        return table;
    }

    @Override
    public void decompress(InputStream input, OutputStream output) throws IOException {
        List<ByteString> table = intializeTable();
        int previousCode = readCode(input);
        if(previousCode < 0) return;
        output.write(previousCode);

        int currentByte = previousCode;
        int currentCode;
        while((currentCode = readCode(input)) >= 0) {
            ByteString pattern = EMPTY;
            if(currentCode >= table.size()) {
                pattern = table.get(previousCode).add(currentByte);
            }
            else {
                pattern = table.get(currentCode);
            }
            output.write(pattern.toByteArray());
            currentByte = pattern.at(0);
            table.add(table.get(previousCode).add(currentByte));
            previousCode = currentCode;
        }
    }
}
