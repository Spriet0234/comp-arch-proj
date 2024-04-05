import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CacheSim {
    public static void main(String[] args) {
        int cacheSize = 0;
        int blockSize = 0;
        int associativity = 0;
        String replacementPolicy = "";
        int physicalMemory = 0;
        int usage = 0;
        int instructions = 0;
        List<String> traceFiles = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                    cacheSize = Integer.parseInt(args[++i]);
                    break;
                case "-b":
                    blockSize = Integer.parseInt(args[++i]);
                    break;
                case "-a":
                    associativity = Integer.parseInt(args[++i]);
                    break;
                case "-r":
                    replacementPolicy = args[++i];
                    break;
                case "-p":
                    physicalMemory = Integer.parseInt(args[++i]);
                    break;
                case "-u":
                    usage = Integer.parseInt(args[++i]);
                    break;
                case "-n":
                    instructions = Integer.parseInt(args[++i]);
                    break;
                case "-f":
                    while(i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        traceFiles.add(args[++i]);
                    }
                    break;
            }
        }

        double total_num_blocks = (cacheSize*1000)/blockSize;

        System.out.println("Trace File(s):");
        for (String file : traceFiles) {
            try {
        // Creating a BufferedReader to read from the file
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        
        // Reading each line of the file until the end
        while ((line = reader.readLine()) != null) {
            // Process each line as needed
            System.out.println(line);
        }
        
        // Closing the BufferedReader
        reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file);
        } catch (IOException e) {
            System.err.println("Error reading from file: " + file);
        }
    }
        System.out.println("");

        int numSets = (cacheSize * 1024) / (blockSize * associativity);
        CacheBlock[][] cache = new CacheBlock[numSets][associativity];


        System.out.println("*****Input Paramenters*****");
        System.out.println("Cache Size: " + cacheSize + " KB");
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Associativity: " + associativity);
        System.out.println("Replacement Policy: " + replacementPolicy);
        System.out.println("Physical Memory: " + physicalMemory + " MB");
        System.out.println("Percent memory used by system: " + usage + "%");
        System.out.println("Instructions / Time Slice: " + instructions);
        System.out.println("");

        System.out.println("Total # blocks: "+ total_num_blocks);


        
    }
}
