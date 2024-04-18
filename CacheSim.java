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

        //Part 2
        long totalAccesses = 0;
        long cacheHits = 0;
        long cacheMisses = 0;
        long compulsoryMisses = 0;
        long conflictMisses = 0;
       

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

         int totalNumRows = cacheSize/(blockSize*associativity);
         System.out.println("Trace File(s):");
         for(String file:traceFiles){
             System.out.println(file);
         }
         System.out.println();

         CacheBlock[][] cache = new CacheBlock[totalNumRows][associativity];
        for (int i = 0; i < totalNumRows; i++) {
            for (int j = 0; j < associativity; j++) {
                cache[i][j] = new CacheBlock(); 
            }
        }

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
                        System.out.println("0x" + Long.toHexString(address) + ": ("+length + ")");

                        //Cache access
                        int index = (int)((address / blockSize) % totalNumRows);
                        long tag = address / (blockSize * totalNumRows);
                        boolean hit = false;
                        for (int j = 0; j < associativity; j++) {
                            if (cache[index][j].isValid() && cache[index][j].getTag() == tag) {
                                cacheHits++;
                                hit = true;
                                break;
                            }
                        }
                        if (!hit) {
                            cacheMisses++;
                            boolean placed = false;
                            for (int j = 0; j < associativity; j++) {
                                if (!cache[index][j].isValid()) {
                                    cache[index][j].setValid(true);
                                    cache[index][j].setTag(tag);
                                    compulsoryMisses++;
                                    placed = true;
                                    break;
                                }
                            }
                            if (!placed) {
                                conflictMisses++;
                                // Simple replacement policy: replace the first block
                                cache[index][0].setTag(tag);;
                            }
                        }
                        totalAccesses++;
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
        //CacheBlock[][] cache = new CacheBlock[numSets][associativity];


        System.out.println("*****Input Paramenters*****");
        System.out.println("Cache Size: " + cacheSize + " KB");
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Associativity: " + associativity);
        System.out.println("Replacement Policy: " + replacementPolicy);
        System.out.println("Physical Memory: " + physicalMemory + " MB");
        System.out.println("Percent memory used by system: " + usage + "%");
        System.out.println("Instructions / Time Slice: " + instructions);
        System.out.println("");

        int totalNumBlocks = (cacheSize * 1024) / blockSize;
        int indexSizeBits = (int)(Math.log(totalNumRows) / Math.log(2));
        int blockOffsetSize = (int)(Math.log(blockSize) / Math.log(2));
        int tagSizeBits = 32 - indexSizeBits - blockOffsetSize;
        int overheadBytes = (totalNumBlocks * (tagSizeBits + 1)) / 8;
        int impMemSizeBytes = (cacheSize * 1024) + overheadBytes;
        double cost = (impMemSizeBytes / 1024.0) * 0.15;

        int pageSizeKB = 4; 
        int numPhysicalPages = (physicalMemory * 1024) / pageSizeKB;
        int numPagesForSystem = (int)((numPhysicalPages * usage)/100);
        int pteSizeBits = 19;
        long totalRamForPageTablesBytes = numPagesForSystem * pteSizeBits ;

        System.out.println("*****Cache Calculated Values*****");
        System.out.println("Total # Blocks: " + totalNumBlocks);
        System.out.println("Tag Size: " + tagSizeBits + " bits");
        System.out.println("Index Size: " + indexSizeBits + " bits");
        System.out.println("Total Number of Rows (Sets): " + totalNumRows);
        System.out.println("Overhead Size: " + overheadBytes + " bytes");
        System.out.println("Implementation Memory Size: " + impMemSizeBytes / 1024.0 + " KB ("+ impMemSizeBytes+" bytes)");
        System.out.println("Cost: $" + String.format("%.2f", cost) + " @ $0.15 / KB\n");
        
        System.out.println("*****Physical Memory Calculated Values*****");
        System.out.println("Number of Physical Pages: " + numPhysicalPages);
        System.out.println("Number of Pages for System: " + numPagesForSystem);
        System.out.println("Size of Page Table Entry: " + pteSizeBits + " bits");
        System.out.println("Total RAM for Page Table(s): " + totalRamForPageTablesBytes + " bytes");

        System.out.println("***** CACHE SIMULATION RESULTS *****");
        System.out.println("Total Cache Accesses: " + totalAccesses);
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("Compulsory Misses: " + compulsoryMisses);
        System.out.println("Conflict Misses: " + conflictMisses);
        double hitRate = ((double)cacheHits / totalAccesses) * 100;
        double missRate = 100 - hitRate;
        System.out.println("Cache Hit Rate: " + String.format("%.4f", hitRate) + "%");
        System.out.println("Cache Miss Rate: " + String.format("%.4f", missRate) + "%");
    

    }
}
