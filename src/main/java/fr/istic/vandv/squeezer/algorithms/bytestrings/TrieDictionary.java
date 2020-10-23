package fr.istic.vandv.squeezer.algorithms.bytestrings;

public class TrieDictionary {

    private int count; // Signals the next index

    private final TrieNode root;

    public TrieDictionary(){
        root = new TrieNode();
        for(int i = 0; i < 256; i++) {
            root.expand(i, i);
        }
        count = 256;
    }

    public int add(ByteString string) {
        if(string == null || string.empty())
            throw new IllegalArgumentException("Input string must not be null or empty");
        TrieNode current = root;
        for(int index = 0; index < string.length(); index++) {
            int value = string.at(index);
            current = current.hasChildrenAt(value)?current.at(value):current.expand(value);
        }
        if(current.hasIndex()) {
            throw new IllegalArgumentException("Dictionary already contains this byte string with index " + current.index);
        }
        current.index = count++;
        return current.index;
    }

    public boolean contains(ByteString string) {
        return indexOf(string) >= 0;
    }

    public int indexOf(ByteString string) {
        if(string == null || string.empty())
            throw new IllegalArgumentException("Input string must not be null or empty");
        TrieNode current = root;
        for(int index = 0; index < string.length(); index++) {
            int value = string.at(index);
            if (!current.hasChildrenAt(value))
                return -1;
            current = current.at(value);
        }
        return current.hasIndex()?current.index:-1;
    }

    public int count() { return count; }

    private static class TrieNode {

        private final TrieNode[] children;
        private Integer index;

        public TrieNode() { children = new TrieNode[256]; }

        public TrieNode(int value) {
            this();
            index = value;
        }

        public boolean hasIndex() { return index != null; }

        public TrieNode at(int index) {
            return children[index];
        }

        public TrieNode expand(int index) {
            return children[index] = new TrieNode();
        }

        public TrieNode expand(int index, int value) {
            return children[index] = new TrieNode(value);
        }

        public boolean hasChildrenAt(int index) {
            return children[index] != null;
        }
    }



}
