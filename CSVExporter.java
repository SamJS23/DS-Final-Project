import java.io.*;
import java.util.Map;

public class CSVExporter {

    public static void exportResults(String filePath, Map<String, Integer> wordCount) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Word,Frequency");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }
}
