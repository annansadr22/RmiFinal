package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface InvertedIndexService extends Remote {
    // Modified to return Map<String, List<Integer>> for line appearances
    Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException;
}


