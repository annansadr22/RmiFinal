package org.example;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class InvertedIndexServiceNaming {
    
    public static void main(String[] args) {
    	
        try {
            InvertedIndexServiceImpl server = new InvertedIndexServiceImpl();
            LocateRegistry.createRegistry(8080);
            
            Naming.rebind("rmi://140.238.158.223:8080/InvertedIndexService", server);
            
            System.out.println("InvertedIndexService ready...");
        } catch (MalformedURLException | RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
