package RMI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileRead {
    public static List<String> readFile(String fileName) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static Map<String, List<Integer>> processLines(List<String> lines, int startingLineNumber) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();
        int lineNumber = startingLineNumber;
        for (String line : lines) {
            String[] words = line.split("\\s+");
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                invertedIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(lineNumber);
            }
            lineNumber++; // Increment the line number for each line processed
        }
        return invertedIndex;
    }
}
