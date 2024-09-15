import java.io.*;
import java.util.*;

public class Main {
    private static final String PATHDB = "db.txt";
    private static final String FILTERED_DB = "filtered_db.txt";
    private static final String BASES = "ACGT";
    private static final Random RANDOM = new Random();
    private static final String MOTIF = "AGCTATC";
    private static final double ENTROPY_THRESHOLD = 1.8;

    public static void main(String[] args) {
        int n = 20000;
        int m = 100;
        int numThreads = 4;

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PATHDB, false));
            writer.close();
        } catch (IOException e) {
            System.out.println("Error deleting file");
        }

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            int start = i * (n / numThreads);
            int end = (i + 1) * (n / numThreads);
            if (i == numThreads - 1) {
                end = n;
            }
            threads[i] = new Thread(new DbWriterTask(start, end, m));
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println("Error waiting for thread");
            }
        }

        System.out.println("Database created.\n");
        SearchMotif(MOTIF, PATHDB);
        filterDatabaseByEntropy(PATHDB, FILTERED_DB, ENTROPY_THRESHOLD);
        System.out.println("\nDatabase filtered:\n");
        SearchMotif(MOTIF, FILTERED_DB);
    }

    static class DbWriterTask implements Runnable {
        private final int start;
        private final int end;
        private final int m;

        DbWriterTask(int start, int end, int m) {
            this.start = start;
            this.end = end;
            this.m = m;
        }

        @Override
        public void run() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATHDB, true))) {
                for (int i = start; i < end; i++) {
                    StringBuilder sb = new StringBuilder(m);
                    for (int j = 0; j < m; j++) {
                        int index = RANDOM.nextInt(BASES.length());
                        char sequence = BASES.charAt(index);
                        sb.append(sequence);
                    }
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            } catch (IOException e) {
                System.out.println("Error writing to file");
            }
        }
        
    }

    public static double calculateEntropy(String sequence) {
        Map<Character, Integer> freqMap = new HashMap<>();
        int length = sequence.length();

        for (char c : sequence.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        for (int freq : freqMap.values()) {
            double probability = (double) freq / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }
        public static void filterDatabaseByEntropy(String inputPath, String outputPath, double threshold) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, false))) {

            String line;
            while ((line = reader.readLine()) != null) {
                double entropy = calculateEntropy(line);
                if (entropy > threshold) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error filtering database");
        }
    }

    public static void SearchMotif(String motif, String path) {
        int count = 0;
        double totalTime = 0.0;
        double firstTime = 0.0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            long start = System.nanoTime();
            while ((line = reader.readLine()) != null) {
                if (line.contains(motif)) {
                    count+=1;
                }
                if (count == 1) {
                    firstTime = (System.nanoTime() - start)/1_000_000_000.0;
                }
            }
            reader.close();
            long end = System.nanoTime();
            totalTime = (end - start)/1_000_000_000.0;

        } catch (IOException e) {
            System.out.println("Error");
        }
        System.out.println("Database size: " + getFileSize(path) + " bytes");
        System.out.println("Motif: " + MOTIF);
        System.out.println("Motif size:" + MOTIF.length());
        System.out.println("Ocurrences: " + count);
        System.out.println("Time to find the first occurrence: " + firstTime + " seconds");
        System.out.println("total execution time:" + totalTime + " seconds");
    }
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }
}




