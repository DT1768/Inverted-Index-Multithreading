package RMI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.*;

public class ImplForkJoinPool extends UnicastRemoteObject implements InvertedIndexService {

    protected ImplForkJoinPool() throws RemoteException {
        super();
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        try {
            List<String> lines = FileRead.readFile(fileName);
            ForkJoinPool pool = new ForkJoinPool();
            InvertedIndexTask task = new InvertedIndexTask(lines, 0, lines.size(), 1); // Start with line offset 1 for the first line
            return pool.invoke(task);
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        }
    }

    private static class InvertedIndexTask extends RecursiveTask<Map<String, List<Integer>>> {
        private final List<String> lines;
        private final int start;
        private final int end;
        private final int offset; // Line offset for correct numbering
        private static final int THRESHOLD = 10;

        public InvertedIndexTask(List<String> lines, int start, int end, int offset) {
            this.lines = lines;
            this.start = start;
            this.end = end;
            this.offset = offset;
        }

        @Override
        protected Map<String, List<Integer>> compute() {
            if (end - start <= THRESHOLD) {
                return processLinesWithOffset(lines.subList(start, end), offset + start);
            } else {
                int mid = start + (end - start) / 2;
                InvertedIndexTask task1 = new InvertedIndexTask(lines, start, mid, offset);
                InvertedIndexTask task2 = new InvertedIndexTask(lines, mid, end, offset);
                invokeAll(task1, task2);
                return mergeIndices(task1.join(), task2.join());
            }
        }

        private Map<String, List<Integer>> processLinesWithOffset(List<String> lines, int offset) {
            Map<String, List<Integer>> invertedIndex = new HashMap<>();
            for (int i = 0; i < lines.size(); i++) {
                String[] words = lines.get(i).split("\\s+");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(i + offset);
                }
            }
            return invertedIndex;
        }

        private Map<String, List<Integer>> mergeIndices(Map<String, List<Integer>> map1, Map<String, List<Integer>> map2) {
            map2.forEach((key, valueList) -> map1.merge(key, valueList, (v1, v2) -> {
                List<Integer> mergedList = new ArrayList<>(v1);
                mergedList.addAll(v2);
                return mergedList;
            }));
            return map1;
        }
    }
}
