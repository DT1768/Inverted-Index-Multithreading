package RMI;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImplExecutor extends UnicastRemoteObject implements InvertedIndexService {

    int threadCount = 4;

    protected ImplExecutor() throws RemoteException {
        super();
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        try {
            List<String> lines = FileRead.readFile(fileName);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<Map<String, List<Integer>>>> futures = new ArrayList<>();

            int segmentSize = (lines.size() + (threadCount - 1)) / threadCount;

            for (int i = 0; i < lines.size(); i += segmentSize) {
                final int start = i;
                final int end = Math.min(i + segmentSize, lines.size());
                final int startingLineNumber = start + 1; // Correct line number
                Callable<Map<String, List<Integer>>> task = () -> FileRead.processLines(lines.subList(start, end), startingLineNumber);
                futures.add(executor.submit(task));
            }

            Map<String, List<Integer>> combinedResults = new HashMap<>();
            for (Future<Map<String, List<Integer>>> future : futures) {
                Map<String, List<Integer>> result = future.get();
                result.forEach((key, valueList) -> combinedResults.merge(key, valueList, (v1, v2) -> {
                    List<Integer> mergedList = new ArrayList<>(v1);
                    mergedList.addAll(v2);
                    return mergedList;
                }));
            }

            executor.shutdown();
            return combinedResults;
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        } catch (Exception e) {
            throw new RemoteException("Error processing file", e);
        }
    }
}
