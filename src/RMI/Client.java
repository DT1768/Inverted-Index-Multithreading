package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public class Client {
    public static void main(String[] args) {
        try {
            // Get registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the remote object "InvertedIndexService" from registry
            InvertedIndexService service = (InvertedIndexService) registry.lookup("InvertedIndexService");

            // Call remote method
            Map<String, List<Integer>> index = service.getInvertedIndex("test.txt");

            // Process and display the results
            index.forEach((key, value) -> System.out.println(key + " found on lines: " + value));
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
