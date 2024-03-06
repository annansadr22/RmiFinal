package org.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


public class InvertedIndexServiceImpl extends UnicastRemoteObject implements InvertedIndexService {
    private ExecutorService executor;
    private static final long serialVersionUID = 1L;

    public InvertedIndexServiceImpl() throws RemoteException {
        super();
        // Initialize the executor with a fixed thread pool size
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    
    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        File file = new File(fileName);
        // Check if file exists
        if (!file.exists()) {
            throw new RemoteException("File not found: " + fileName);
        }

        // Create a map to store the inverted index
        final Map<String, List<Integer>> index = new HashMap<>();

        try (Scanner scanner = new Scanner(file)) {
            int lineNum = 1;
            // Process each line in the file
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                if (executor.isTerminated()) {
                    // Reinitialize the executor service if it has been shut down
                    executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                }
                
                // Submit a task to the executor to compute the inverted index for each line
                executor.submit(new InvertedIndexTask(line, lineNum, index));
                lineNum++;
            }
        } catch (FileNotFoundException e) {
            throw new RemoteException("Error reading file: " + fileName, e);
        } finally {
            // Shutdown the executor after processing all tasks
            shutdownExecutor();
        }

        return getTopWords(index);
    }


    private static class InvertedIndexTask implements Runnable {
        private final String line;
        private final int lineNum;
        private final Map<String, List<Integer>> index;

        public InvertedIndexTask(String line, int lineNum, Map<String, List<Integer>> index) {
            this.line = line;
            this.lineNum = lineNum;
            this.index = index;
        }

        @Override
        public void run() {
            // Split the line into words
            String[] words = line.split("\\s+");
            synchronized (index) {
                for (String word : words) {
                    // Update the inverted index
                    index.computeIfAbsent(word, k -> new ArrayList<>()).add(lineNum);
                }
            }
        }
    }
    
    
    private Map<String, List<Integer>> getTopWords(Map<String, List<Integer>> index) {
        // Sort the words by their occurrences in descending order
        List<Map.Entry<String, List<Integer>>> sortedEntries = index.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());

        // Get the top 5 words with the most appearances
        Map<String, List<Integer>> topWords = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(5, sortedEntries.size()); i++) {
            Map.Entry<String, List<Integer>> entry = sortedEntries.get(i);
            topWords.put(entry.getKey(), entry.getValue());
        }
        return topWords;
    }
    
    public void shutdownExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

