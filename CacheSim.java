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
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.charAt(0) == 'd'){
                        continue;
                    }
                    if (line.length() > 17) {
                        String lengthHex = line.substring(5, 7).trim(); 
                        int length = Integer.parseInt(lengthHex, 16); 
                        
                        String addressHex = line.substring(10, 18);
                        long address = Long.parseLong(addressHex, 16); 
        
                        System.out.println("Length: " + length + ", Address: 0x" + Long.toHexString(address));
                    }
                }
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
