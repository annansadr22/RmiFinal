package org.example;

import java.rmi.Naming;
import java.util.List;
import java.util.Map;

public class InvertedIndexServiceClient {

    public static void main(String[] args) {
        try {
            // Look up the remote object from the RMI registry
            InvertedIndexService service = (InvertedIndexService) Naming.lookup("rmi://140.238.158.223:8080/InvertedIndexService");

            // Example usage: Get the inverted index for a text
            String fileName = "sample_data.txt";
            
            Map<String, List<Integer>> invertedIndex = service.getInvertedIndex(fileName);

            // Print the inverted index
            System.out.println("Inverted Index:");
            for (Map.Entry<String, List<Integer>> entry : invertedIndex.entrySet()) {
                System.out.print(entry.getKey() + ": ");
                for (Integer line : entry.getValue()) {
                    System.out.print(line + " ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
