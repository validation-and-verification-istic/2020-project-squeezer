package fr.istic.vandv.squeezer;


import fr.istic.vandv.squeezer.algorithms.CompressionAlgorithm;
import fr.istic.vandv.squeezer.algorithms.HuffmanEncoding;
import fr.istic.vandv.squeezer.algorithms.LZWCompression;
import fr.istic.vandv.squeezer.algorithms.RunLengthCompression;
import picocli.CommandLine;
import picocli.CommandLine.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Command(
        name="squeezer",
        description = "Simple application to compress and decompress a given file.",
        mixinStandardHelpOptions = true,
        subcommands = {HelpCommand.class}
)
public class Application {


    private static final byte[] SQZ_HEADER = { 83, 81, 90 };

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Application());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        System.exit(cmd.execute(args));
    }

    @Command(name="compress", aliases = {"comp", "cp" , "c"}, description = "Compresses a given file using the specified algorithm.")
    public int compress(
            @Option(names = {"--use", "-u"}, paramLabel = "ALGORITHM", defaultValue = "LZW", description = "Algorithm to compress the given file. Valid values: ${COMPLETION-CANDIDATES})" )
            AlgorithmInformation algorithm,
            @Parameters(description = "File to compress. It must exists and be readable.")
            File input,
            @Parameters(description = "Path where the compressed file should be created. The file should not exist and the location must be writable.")
            File output) {

        try {

            int validation = validateArguments(input, output);
            if(ExitCodes.isError(validation)) {
                return validation;
            }

            FileOutputStream outputStream = new FileOutputStream(output);
            outputStream.write(SQZ_HEADER);
            outputStream.write(algorithm.opcode);

            FileInputStream inputStream = new FileInputStream(input);

            CompressionAlgorithm implementation = algorithm.getInstance();

            implementation.compress(inputStream, outputStream);

            inputStream.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException exc) {
            System.err.println("Unexpected I/O exception: " + exc.getMessage());
            return ExitCodes.IO_ERROR;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException exc) {
            System.err.println("Unexcepted error: " + exc.getMessage());
            return ExitCodes.SOFTWARE_ERROR;
        }

        return ExitCodes.SUCCESS;
    }

    private int validateArguments(File input, File output) throws IOException {
        if (!input.exists()) {
            System.err.println("Input file does not exist: " + input.getAbsolutePath());
            return ExitCodes.NO_INPUT;
        }

        if (!input.isFile()) {
            System.err.println("Input path is not a file : " + input.getAbsolutePath());
            return ExitCodes.NO_INPUT;
        }

        if (!input.canRead()) {
            System.err.println("Input file is not readable: " + input.getAbsolutePath());
            return ExitCodes.NO_INPUT;
        }

        if (!output.createNewFile()) {
            System.err.println("Output file already exists: " + output.getAbsolutePath());
            return ExitCodes.CANT_CREATE;
        }

        return ExitCodes.SUCCESS;

    }

    @Command(name="decompress", aliases = {"dcmp", "dc", "d"}, description = "Decompresses a given previously compressed file.")
    public int decompress(File input, File output) {
        try {

            int validationCode = validateArguments(input, output);
            if(ExitCodes.isError(validationCode)) {
                return validationCode;
            }

            FileInputStream inputStream = new FileInputStream(input);

            byte[] headerInFile = new byte[SQZ_HEADER.length];
            inputStream.read(headerInFile);

            if(!Arrays.equals(headerInFile, SQZ_HEADER)) {
                System.err.println("Bad file input format. Should start by SQZ.");
                return ExitCodes.NO_INPUT;
            }

            int opcode = inputStream.read();
            AlgorithmInformation algorithm = AlgorithmInformation.fromOpcode(opcode);

            if(algorithm == null) {
              System.err.println("Wron file input format. Wrong algorithm opcode: " + opcode);
              return ExitCodes.NO_INPUT;
            }

            FileOutputStream outputStream = new FileOutputStream(output);
            CompressionAlgorithm implementation = algorithm.getInstance();
            implementation.decompress(inputStream, outputStream);
            inputStream.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException exc) {
            System.err.println("Unexpected I/O exception: " + exc.getMessage());
            return ExitCodes.IO_ERROR;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException exc) {
            System.err.println("Unexcepted error: " + exc.getMessage());
            return ExitCodes.SOFTWARE_ERROR;
        }

        return ExitCodes.SUCCESS;
    }

    @Command(name="algorithms", aliases = {"algs", "al", "a"}, description = "Shows a list of all algorithms included in the application.")
    public void listOfAlgorithms() {
        for(AlgorithmInformation info : AlgorithmInformation.values()) {
            System.out.println(info.name() + ": " + info.description);
        }
    }

    enum AlgorithmInformation {
        RUN_LENGTH(2,"Run-length compression algorithm", RunLengthCompression.class),
        HUFFMAN (1, "Huffman compression algorithm", HuffmanEncoding.class),
        LZW (0, "Lempel-Ziv-Welch compression algorithm",LZWCompression.class);

        int opcode;
        String description;
        Class<? extends CompressionAlgorithm> implementation;

        AlgorithmInformation(int opcode, String description, Class<? extends CompressionAlgorithm> implementation) {
            this.opcode = opcode;
            this.description = description;
            this.implementation = implementation;
        }

        public CompressionAlgorithm getInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return implementation.getConstructor().newInstance();
        }

        public static AlgorithmInformation fromOpcode(int opcode) {
            for(AlgorithmInformation info : AlgorithmInformation.values()) {
                if(opcode == info.opcode)
                    return info;
            }
            return null;
        }

    }


    static class ExitCodes {
        private ExitCodes() {}
        public static final int SUCCESS = 0;
        public static final int NO_INPUT = 64;
        public static final int SOFTWARE_ERROR = 70;
        public static final int CANT_CREATE = 73;
        public static final int IO_ERROR = 74;

        public static boolean isError(int code) {
            return code != 0;
        }
    }
}
