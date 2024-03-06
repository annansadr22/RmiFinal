package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
// import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
// import java.util.concurrent.RecursiveTask;

public class InvertedIndexServiceImpl extends UnicastRemoteObject implements InvertedIndexService {
    private static final long serialVersionUID = 1L;
    private ForkJoinPool pool;

    public InvertedIndexServiceImpl() throws RemoteException {
        super();
        pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        Map<String, List<Integer>> index = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            int lineNum = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                if (pool.isShutdown()) {
                    // Reinitialize the pool if it has been shut down
                	pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                }
                
                pool.submit(new LineIndexTask(line, lineNum, index)).get();
                lineNum++;
            }
        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown(); // Shut down the pool
        }
        
        return getTopWords(index);
    }


    private static class LineIndexTask implements Callable<Void> {
        private final String line;
        private final int lineNum;
        private final Map<String, List<Integer>> index;

        public LineIndexTask(String line, int lineNum, Map<String, List<Integer>> index) {
            this.line = line;
            this.lineNum = lineNum;
            this.index = index;
        }

        @Override
        public Void call() {
            String[] words = line.split("\\s+");
            for (String word : words) {
                synchronized (index) {
                    index.computeIfAbsent(word, k -> new ArrayList<>()).add(lineNum);
                }
            }
            return null;
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

}
