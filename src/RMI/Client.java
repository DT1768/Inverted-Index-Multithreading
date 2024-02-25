package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            List<Map.Entry<String, List<Integer>>> sortedEntries = index.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparingInt(List::size))))
                    .limit(5)
                    .collect(Collectors.toList());

            // Process and display the results
            System.out.println("Top 5 most frequent words and their locations:");
            for (Map.Entry<String, List<Integer>> entry : sortedEntries) {
                System.out.println(entry.getKey() + " found on lines: " + entry.getValue());
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
