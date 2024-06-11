import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class WordFrequencyAnalyzerHashMap extends JFrame {

    private static Map<String, Integer> cumulativeWordCount = new HashMap<>();
    private static List<String> sentences = new ArrayList<>();

    public static void main(String[] args) {
        boolean running = true;
        Scanner scanner = new Scanner(System.in);

        while (running) {
            try {
                System.out.println("Choose one of the options: ");
                System.out.println("1. Insert user inputted sentences");
                System.out.println("2. Text File");
                System.out.println("3. View all words");
                System.out.println("4. Edit Sentence");
                System.out.println("5. Remove Sentence");
                System.out.println("6. Export Results to CSV");
                System.out.println("7. Display Bar Chart");
                System.out.println("8. Exit");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline left-over

                switch (choice) {
                    case 1:
                        System.out.println("Enter your text:");
                        String userInput = scanner.nextLine();
                        String[] splitSentencesInUser = userInput.split("(?<=\\p{Punct})\\s*(?=\\p{L})|\\n+");

// Add each split sentence to the sentences list
                        sentences.addAll(Arrays.asList(splitSentencesInUser));

                        Map<String, Integer> userWordCount = countWordFrequency(userInput);
                        printWordFrequency(userWordCount);
                        addWordCountToCumulative(userWordCount);
                        break;
                    case 2:
                        System.out.println("Enter file path:");
                        String filePath = scanner.nextLine();
                        String fileContent = FileProcessor.readFile(filePath);
                        String[] splitSentencesInFile = fileContent.split("(?<=\\p{Punct})\\s*(?=\\p{L})|\\n+");

// Add each split sentence to the sentences list
                        sentences.addAll(Arrays.asList(splitSentencesInFile));
                        Map<String, Integer> fileWordCount = countWordFrequency(fileContent);
                        printWordFrequency(fileWordCount);
                        addWordCountToCumulative(fileWordCount);
                        break;
                    case 3:
                        printWordFrequency(cumulativeWordCount);
                        break;
                    case 4:
                        editSentence(scanner);
                        break;
                    case 5:
                        removeSentence(scanner);
                        break;
                    case 6:
                        System.out.println("Enter file path to export:");
                        String exportFilePath = scanner.nextLine();
                        CSVExporter.exportResults(exportFilePath, cumulativeWordCount);
                        System.out.println("Export successful to: " + exportFilePath);
                        break;
                    case 7:
                        if (cumulativeWordCount.isEmpty()) {
                            System.out.println("No data to display in the chart.");
                        } else {
                            displayWordFrequencyChart(cumulativeWordCount);
                        }
                        break;
                    case 8:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer choice.");
                scanner.nextLine();  // Consume the invalid input
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }

        scanner.close(); // Close the scanner when done
    }


    private static void editSentence(Scanner scanner) {
        if (sentences.isEmpty()) {
            System.out.println("No sentences to edit.");
            return;
        }

        System.out.println("Choose a sentence to edit:");
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            System.out.println((i + 1) + ". " + sentence.trim());
        }

        int choice = -1;
        while (true) {
            System.out.print("Enter the sentence number to edit: ");
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > sentences.size()) {
                    System.out.println("Invalid choice. Please enter a valid sentence number.");
                } else {
                    break; // Exit the loop if the choice is valid
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }

        System.out.println("Enter the new sentence:");
        String newSentence = scanner.nextLine();

        // Update the sentence in the sentences list and cumulativeWordCount
        String oldSentence = sentences.get(choice - 1);

        // Update the cumulative word count before replacing the sentence
        Map<String, Integer> wordCountToRemove = countWordFrequency(oldSentence);
        removeWordCountFromCumulative(wordCountToRemove);

        // Replace the sentence
        sentences.set(choice - 1, newSentence);

        // Update the cumulative word count with the new sentence
        Map<String, Integer> wordCountToAdd = countWordFrequency(newSentence);
        addWordCountToCumulative(wordCountToAdd);

        System.out.println("Sentence edited successfully.");
    }

    private static void removeSentence(Scanner scanner) {
        if (sentences.isEmpty()) {
            System.out.println("No sentences to remove.");
            return;
        }

        System.out.println("Choose a sentence to remove:");
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            System.out.println((i + 1) + ". " + sentence.trim());
        }

        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline left-over from nextInt()

        int index = 0;
        for (String sentence : sentences) {{
                if (!sentence.isEmpty()) {  // Ignore empty sentences
                    index++;
                    if (index == choice) {
                        Map<String, Integer> wordCountToRemove = countWordFrequency(sentence);
                        sentences.remove(sentence);
                        removeWordCountFromCumulative(wordCountToRemove);
                        System.out.println("Sentence removed.");
                        return;
                    }
                }
            }
        }

        System.out.println("Invalid choice.");
    }

    private static Map<String, Integer> countWordFrequency(String text) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        return wordCount;
    }

    private static void printWordFrequency(Map<String, Integer> wordCount) {
        wordCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    private static void addWordCountToCumulative(Map<String, Integer> wordCount) {
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            cumulativeWordCount.put(entry.getKey(), cumulativeWordCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    private static void removeWordCountFromCumulative(Map<String, Integer> wordCount) {
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            int count = entry.getValue();
            cumulativeWordCount.put(word, cumulativeWordCount.getOrDefault(word, 0) - count);
            if (cumulativeWordCount.get(word) <= 0) {
                cumulativeWordCount.remove(word);
            }
        }
    }

    private static void displayWordFrequencyChart(Map<String, Integer> wordFrequency) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            dataset.addValue(entry.getValue(), "Frequency", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Word Frequency Bar Chart",
                "Word",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        JFrame frame = new JFrame("Word Frequency Bar Chart");

        // Create a ChartPanel and set preferred size
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600)); // Adjust size as needed

        // Add ChartPanel to the frame's content pane
        frame.setContentPane(chartPanel);

        // Set frame properties
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack(); // Pack components to set preferred sizes
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true); // Make the frame visible
    }
}
