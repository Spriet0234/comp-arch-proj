import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CacheSim {
    static CacheBlock[][] cache; 

    static int cacheSize = 0;
    static int blockSize = 0;
    static int associativity = 0;
    static String replacementPolicy = "";
    static int physicalMemory = 0;
    static int usage = 0;
    static int instructions = 0;
    static long totalAccesses = 0;
    static long cacheHits = 0;
    static long cacheMisses = 0;
    static long compulsoryMisses = 0;
    static long conflictMisses = 0;
    static int totalNumRows;

    static long instructionBytes = 0;
    static long srcDstBytes = 0;
    

    static int totalNumBlocks;
    static int tagSizeBits;
    static int indexSizeBits;
    static int overheadBytes;
    static int impMemSizeBytes;
    static double cost;
    static int numPhysicalPages;
    static int numPagesForSystem;
    static int pteSizeBits;
    static long totalRamForPageTablesBytes;

    //for rr
    static int[] nextReplaceIndex; 

    public static void main(String[] args) {
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

        
        totalNumBlocks = (cacheSize * 1024) / blockSize;
        totalNumRows = totalNumBlocks / associativity;
        indexSizeBits = (int) (Math.log(totalNumRows) / Math.log(2));
        int blockOffsetSize = (int) (Math.log(blockSize) / Math.log(2));
        tagSizeBits = 32 - indexSizeBits - blockOffsetSize;
        overheadBytes = (totalNumBlocks * (tagSizeBits + 1)) / 8;
        impMemSizeBytes = (cacheSize * 1024) + overheadBytes;
        cost = (impMemSizeBytes / 1024.0) * 0.15;

        nextReplaceIndex = new int[totalNumRows];  // One entry per set, initially all zero

        int pageSizeKB = 4; 
        int numPhysicalPages = (physicalMemory * 1024) / pageSizeKB;
        int numPagesForSystem = (int)((numPhysicalPages * usage)/100);
        int pteSizeBits = 19;
        long totalRamForPageTablesBytes = numPagesForSystem * pteSizeBits ;

        totalNumRows = cacheSize / (blockSize * associativity);
        cache = new CacheBlock[totalNumRows][associativity];
        for (int i = 0; i < totalNumRows; i++) {
            for (int j = 0; j < associativity; j++) {
                cache[i][j] = new CacheBlock();
            }
        }

        for (String file : traceFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("EIP")) {
                        String lengthHex = line.substring(5, 7); 
                        int length = Integer.parseInt(lengthHex, 16); 
                        String addressHex = line.substring(10, 18); 
                        long address = Long.parseLong(addressHex, 16); 
        
                        //System.out.println("0x" + Long.toHexString(address) + ": (" + length + " bytes)");
                        simulateCacheAccess(address, length, true);  // True indicates an instruction access
                        instructionBytes += length;
                        if (line.contains("dstM:") || line.contains("srcM:")) {
                            String[] parts = line.split("\\s+");
                            for (String part : parts) {
                                if (part.startsWith("dstM:") || part.startsWith("srcM:")) {
                                    String addrHex = part.substring(part.indexOf(':') + 1).trim();
                                    if (!addrHex.equals("00000000") && !addrHex.contains("--------")) {
                                        long dataAddress = Long.parseLong(addrHex, 16);
                                        //System.out.println("Data access at 0x" + Long.toHexString(dataAddress) + ": (4 bytes)");
                                        simulateCacheAccess(dataAddress, 4, false);
                                        srcDstBytes += 4;
                                    }
                                }
                            }
                        }
                    }
                }
            }
                        
                    
                 catch (FileNotFoundException e) {
                System.err.println("File not found: " + file);
            } catch (IOException e) {
                System.err.println("Error reading from file: " + file);
            }
        }
        
        System.out.println("*****Input Paramenters*****");
        System.out.println("Cache Size: " + cacheSize + " KB");
        System.out.println("Block Size: " + blockSize + " bytes");
        System.out.println("Associativity: " + associativity);
        System.out.println("Replacement Policy: " + replacementPolicy);
        System.out.println("Physical Memory: " + physicalMemory + " MB");
        System.out.println("Percent memory used by system: " + usage + "%");
        System.out.println("Instructions / Time Slice: " + instructions);
        System.out.println("");
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

        System.out.println("\n***** CACHE SIMULATION RESULTS *****");
        System.out.println("Total Cache Accesses: " + totalAccesses);
        System.out.println("Instruction Bytes:"+instructionBytes+" SrcDst Bytes:" + srcDstBytes);
        System.out.println("Cache Hits: " + cacheHits);
        System.out.println("Cache Misses: " + cacheMisses);
        System.out.println("Compulsory Misses: " + compulsoryMisses);
        System.out.println("Conflict Misses: " + conflictMisses);
        double hitRate = ((double)cacheHits / totalAccesses) * 100;
        double missRate = 100 - hitRate;
        System.out.println("Cache Hit Rate: " + String.format("%.4f", hitRate) + "%");
        System.out.println("Cache Miss Rate: " + String.format("%.4f", missRate) + "%");
    }
        

    private static void simulateCacheAccess(long address, int bytes, boolean isInstruction) {
        int index = (int)((address / blockSize) % totalNumRows);
        long tag = address / (blockSize * totalNumRows);
        boolean hit = false;
        for (int j = 0; j < associativity; j++) {
            if (cache[index][j].isValid() && cache[index][j].getTag() == tag) {
                if (isInstruction) {
                    cacheHits++;  
                }
                hit = true;
                break;
            }
        }
        if (!hit) {
            cacheMisses++;
            int j = findReplacementIndex(index); 
            cache[index][j].setValid(true);
            cache[index][j].setTag(tag);
            compulsoryMisses++;
        }
        totalAccesses++;
    }

    private static int findReplacementIndex(int setIndex) {
        int replaceIndex = nextReplaceIndex[setIndex];
        nextReplaceIndex[setIndex] = (replaceIndex + 1) % associativity;  
        return replaceIndex;
    }
    
    
}
