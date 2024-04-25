import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CacheSim {
    static CacheBlock[][] cache; 

    static long totalCycles = 0;
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


        int instructionCount = 0;
        
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

        //totalNumRows = cacheSize / (blockSize * associativity);
        cache = new CacheBlock[totalNumRows][associativity];
        for (int i = 0; i < totalNumRows; i++) {
            for (int j = 0; j < associativity; j++) {
                cache[i][j] = new CacheBlock();
            }
        }
        long cycles = 0;

        for (String file : traceFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    //  for (int i = 0; i < totalNumRows; i++) { // Iterate through each set
                    //      System.out.println("Set " + i + ":");
                    //      for (int j = 0; j < cache[i].length; j++) { // Iterate through each way in the set
                    //          System.out.println("  Way " + j + ": " + cache[i][j]);
                    //      }
                    //  }
                    
                    if (line.length() >0 && line.charAt(0) == 'E') {
                        
                        String lengthStr = line.substring(5, 7);
                        int length = Integer.parseInt(lengthStr);
                        String addressHex = line.substring(10, 18);
                        long address = Long.parseLong(addressHex, 16);
                        //System.out.println(addressHex);
                        
    
                        totalCycles += simulateCacheAccess(address, length, true); 
                        //2 cycles for instruction
                        totalCycles+=2;
                        instructionCount +=1;
    
                    }else if (line.length() >0 && line.charAt(0) == 'd') {
                        String dstm = line.substring(6,14);
                        String srcm = line.substring(33,41);

                        if (!dstm.equals("00000000")){
                            long dataAddress = Long.parseLong(dstm, 16);
                            totalCycles += simulateCacheAccess(dataAddress, 4, false);
                            totalCycles+=1;
                        }
                        if (!srcm.equals("00000000")){
                            long dataAddress = Long.parseLong(srcm, 16);
                            totalCycles += simulateCacheAccess(dataAddress, 4, false);
                            totalCycles+=1;

                        }        
                                    
                    }
                                
                }   
                
            } catch (IOException e) {
                e.printStackTrace();
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
        System.out.println("--- Compulsory Misses: " + compulsoryMisses);
        System.out.println("--- Conflict Misses: " + conflictMisses);

        double hitRate = ((double)cacheHits / totalAccesses) * 100;
        double missRate = 100 - hitRate;
        double CPI = totalCycles/instructionCount;

        double unusedKB = ((totalNumBlocks-compulsoryMisses)*(blockSize)) / 1024;
        double waste = .15 * unusedKB;
        

        System.out.println("\n***** ***** CACHE HIT AND MISS RATE ***** *****");
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
        System.out.println("Miss Rate: " + String.format("%.2f", missRate) + "%");
        System.out.println("CPI: " + CPI);
        System.out.println("Unused Cache Space: " +unusedKB + " / "+ cacheSize +" = "+String.format("%.2f", unusedKB/cacheSize*100) + "%  Waste: $" + String.format("%.2f", waste));
        System.out.println("Unused Cache Blocks: " + compulsoryMisses + " / "+totalNumBlocks);

    }
        

    private static long simulateCacheAccess(long address, int length, boolean isInstruction) {
        int offsetBits = (int) (Math.log(blockSize) / Math.log(2));  
        int indexBits = (int) (Math.log(totalNumRows) / Math.log(2)); 
    
        int index = (int) ((address >> offsetBits) & ((1 << indexBits) - 1));
        long tag = address >>> (offsetBits + indexBits);
    
        boolean hit = false;
        boolean allBlocksValid = true;
        long cycles = 0;
    
        for (int j = 0; j < associativity; j++) {
            if (cache[index][j].isValid() && cache[index][j].getTag() == tag) {
                // Hit 
                if(isInstruction) {
                    instructionBytes += length;
                } else {
                    srcDstBytes += length;
                }
                cacheHits++;
                hit = true;
                cycles = 1; // +1 cycle for cache hit
                break;
            }
            if (!cache[index][j].isValid()) {
                allBlocksValid = false;
            }
        }
    
        if (!hit) {
            cacheMisses++;
            int j = findReplacementIndex(index);
            boolean wasValid = cache[index][j].isValid();
            cache[index][j].setValid(true);
            cache[index][j].setTag(tag);
    
            if (!wasValid) {
                compulsoryMisses++;
            } else if (allBlocksValid) {
                conflictMisses++; 
            }
    
            int numberOfReads = (int) Math.ceil((double) blockSize / 4);
            cycles = 4 * numberOfReads; // 4 cycles per memory read miss
        }
    
        totalAccesses++;
        return cycles;
    }
    
    private static int findReplacementIndex(int setIndex) {
        int replaceIndex = nextReplaceIndex[setIndex];
        nextReplaceIndex[setIndex] = (replaceIndex + 1) % associativity;
        return replaceIndex;
    }
}
