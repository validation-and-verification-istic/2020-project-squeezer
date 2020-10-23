package fr.istic.vandv.squeezer.algorithms;

import fr.istic.vandv.squeezer.algorithms.bitmanipulation.BitString;
import fr.istic.vandv.squeezer.algorithms.bitmanipulation.BitStringWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class HuffmanEncoding implements CompressionAlgorithm {
    @Override
    public void compress(InputStream input, OutputStream output) throws IOException {
        byte[] content = input.readAllBytes();
        BitString[] table = computeTable(content);
        writeTable(table, output);
        writeContent(table, content, output);
    }

    private void writeContent(BitString[] table, byte[] content, OutputStream output) throws IOException {
        BitStringWriter writer = new BitStringWriter(output);
        for(byte value : content) {
            BitString toWrite = table[Byte.toUnsignedInt(value)];
            writer.write(toWrite);
        }
        int extraBits = writer.flush();
        int padding = extraBits == 0? 0: 8 - extraBits;
        output.write((byte)padding);
    }

    private int[] computeFrequencies(byte[] content) {
        int[] frequencies = new int[256];
        for (byte value : content) {
            frequencies[Byte.toUnsignedInt(value)]++;
        }
        return frequencies;
    }

    private BitString[] computeTable(byte[] content) {
        int[] frequencies = computeFrequencies(content);
        Node tree = buildTree(frequencies);
        return buildTable(tree);
    }

    private Node buildTree(int[] frequencies) {
        PriorityQueue<Node> heap = new PriorityQueue<>(255,  (a, b) -> a.weight - b.weight);
        for (int i = 0; i < frequencies.length; i++) {
            if(frequencies[i] == 0) continue;
            heap.add(new Node(i, frequencies[i]));
        }
        while (heap.size() > 1) {
            heap.add(new Node(heap.remove(), heap.remove()));
        }
        return heap.remove();
    }

    private BitString[] buildTable(Node root) {
        BitString[] table = new BitString[256];
        if(root.isLeaf()) {
            table[root.value] = new BitString().appendOne();
        }
        else {
            fillTable(table, root, new BitString());
        }
        return table;
    }

    private void fillTable(BitString[] table, Node node, BitString current) {
        if(node.isLeaf()) {
            table[node.value] = current;
            return;
        }
        fillTable(table, node.left, current.appendZero());
        fillTable(table, node.left, current.appendOne());
    }

    public void writeTable(BitString[] table, OutputStream output) throws IOException {
        long entries = Arrays.stream(table).filter(Objects::nonNull).count();
        output.write((byte)entries);
        for(int i = 0; i < table.length; i++) {
            if(table[i] == null) continue;
            output.write((byte)i);
            output.write((byte)table[i].length()); // Expecting no string will be larger than 255
            output.write(table[i].toByteArray());
        }
    }

    @Override
    public void decompress(InputStream input, OutputStream output) throws IOException {
        Map<BitString, Byte> str2byte = getMapping(input);
        BitString bt = new BitString();
        byte[] content = input.readAllBytes();
        int padding = content[content.length-1];
        for(int i = 0; i < content.length - 1; i++) {
            int bits = (i < content.length - 2)?8: 8 - padding;
            for (int b = 0; b < bits; b++) {
                if((content[i] & 128) == 0) {
                    bt = bt.appendZero();
                }
                else{
                    bt = bt.appendOne();
                }
                if (str2byte.containsKey(bt)) {
                    output.write(str2byte.get(bt));
                    bt = new BitString();
                }
                content[i] <<= 1;
            }
        }
    }

    private Map<BitString, Byte> getMapping(InputStream input) throws IOException {
        BitString[] table = readTable(input);
        Map<BitString, Byte> str2byte = new HashMap<>();

        for(int i = 0; i < table.length; i++) {
            if(table[i] == null) continue;
            str2byte.put(table[i], (byte)i);
        }
        return str2byte;
    }

    private BitString[] readTable(InputStream input) throws IOException {
        BitString[] table = new BitString[256];
        int entries = input.read();
        for(int i = 0; i < entries; i++) {
            int element = input.read();
            int bits = input.read();
            byte[] str = input.readNBytes(BitString.requiredLenghtForBits(bits));
            table[element] = new BitString(str, bits);
        }
        return table;
    }

    protected static class Node {

        int value;
        int weight;

        Node right;
        Node left;

        public boolean isLeaf() { return right == null && left == null; }

        public Node(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }

        public Node(Node left, Node right) {
            this.left = left;
            this.right = right;
            this.weight = left.weight + right.weight;
        }
    }
}
