package org.example;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InvertedIndexServiceClientPerformance {

    public static void main(String[] args) {
        try {
            // Look up the remote object from the RMI registry
            InvertedIndexService service = (InvertedIndexService) Naming.lookup("rmi://140.238.158.223:8080/InvertedIndexService");

            // Example usage: Get the inverted index for a text
            String fileName = "sample_data.txt";

            // Number of times to run the code
            int numOfTimes = 1000;

            // Lists to store response times
            List<Long> responseTimes = new ArrayList<>();

            // Run the code multiple times
            for (int i = 0; i < numOfTimes; i++) {
                long startTime = System.currentTimeMillis();
                Map<String, List<Integer>> invertedIndex = service.getInvertedIndex(fileName);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                responseTimes.add(responseTime);
            }

            // Calculate metrics
            long mean = calculateMean(responseTimes);
            long median = calculateMedian(responseTimes);
            long min = Collections.min(responseTimes);
            long max = Collections.max(responseTimes);
            long p99 = calculateP99(responseTimes);
            double std = calculateStd(responseTimes);
            long totalResponse = responseTimes.stream().mapToLong(Long::valueOf).sum();
            double throughput = (double) numOfTimes / (totalResponse / 1000.0); // In seconds

            // Print metrics
            System.out.println("Mean: " + mean);
            System.out.println("Median: " + median);
            System.out.println("Min: " + min);
            System.out.println("Max: " + max);
            System.out.println("P99: " + p99);
            System.out.println("Std: " + std);
            System.out.println("Total response: " + totalResponse);
            System.out.println("Throughput: " + throughput + " requests per second");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to calculate the mean
    private static long calculateMean(List<Long> responseTimes) {
        long sum = responseTimes.stream().mapToLong(Long::valueOf).sum();
        return sum / responseTimes.size();
    }

    // Method to calculate the median
    private static long calculateMedian(List<Long> responseTimes) {
        Collections.sort(responseTimes);
        int middle = responseTimes.size() / 2;
        if (responseTimes.size() % 2 == 1) {
            return responseTimes.get(middle);
        } else {
            return (responseTimes.get(middle - 1) + responseTimes.get(middle)) / 2;
        }
    }

    // Method to calculate the 99th percentile
    private static long calculateP99(List<Long> responseTimes) {
        Collections.sort(responseTimes);
        int p99Index = (int) Math.ceil(0.99 * responseTimes.size());
        return responseTimes.get(p99Index - 1);
    }

    // Method to calculate the standard deviation
    private static double calculateStd(List<Long> responseTimes) {
        double mean = calculateMean(responseTimes);
        double sum = 0;
        for (long responseTime : responseTimes) {
            sum += Math.pow(responseTime - mean, 2);
        }
        double variance = sum / responseTimes.size();
        return Math.sqrt(variance);
    }
}

