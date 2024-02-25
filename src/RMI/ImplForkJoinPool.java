package RMI;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ImplForkJoinPool extends UnicastRemoteObject implements InvertedIndexService {

    protected ImplForkJoinPool() throws RemoteException {
        super();
    }

    @Override
    public Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException {
        try {
            List<String> lines = FileRead.readFile(fileName);
            ForkJoinPool pool = new ForkJoinPool();
            InvertedIndexTask task = new InvertedIndexTask(lines, 0, lines.size());
            return pool.invoke(task);
        } catch (IOException e) {
            throw new RemoteException("Error reading file", e);
        }
    }

    private static class InvertedIndexTask extends RecursiveTask<Map<String, List<Integer>>> {
        private final List<String> lines;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 1;

        public InvertedIndexTask(List<String> lines, int start, int end) {
            this.lines = lines;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Map<String, List<Integer>> compute() {
            if (end - start <= THRESHOLD) {
                return FileRead.processLines(lines.subList(start, end));
            } else {
                System.out.println("Fork");
                int mid = start + (end - start) / 2;
                InvertedIndexTask task1 = new InvertedIndexTask(lines, start, mid);
                InvertedIndexTask task2 = new InvertedIndexTask(lines, mid, end);
                invokeAll(task1, task2);
                return mergeIndices(task1.join(), task2.join());
            }
        }

        private Map<String, List<Integer>> mergeIndices(Map<String, List<Integer>> map1, Map<String, List<Integer>> map2) {
            map2.forEach((key, value) -> map1.merge(key, value, (v1, v2) -> {
                List<Integer> merged = new ArrayList<>(v1);
                merged.addAll(v2);
                return merged;
            }));
            return map1;
        }
    }
}
