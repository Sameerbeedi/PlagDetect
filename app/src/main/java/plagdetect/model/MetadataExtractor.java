package plagdetect.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetadataExtractor {
    private static final String SUB_DIR = "submissions";

    public static void main(String[] args) {
        try {
            List<File> submissions = getSubmissions(); // Get all files in submissions directory
            
            if (submissions.isEmpty()) {
                System.out.println("No submissions found.");
                return;
            }
            
            System.out.println("Found " + submissions.size() + " submissions.");

            List<Map<String, String>> fileMetadata = extractFileMetadata(submissions);
            
            sendToDatabase(fileMetadata);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<File> getSubmissions() throws IOException {
        Path subPath = Paths.get(SUB_DIR);

        if (!Files.exists(subPath)) {
            System.err.println("Submissions directory does not exist");
            return new ArrayList<>();
        }

        return Files.list(subPath)
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .collect(Collectors.toList());
    }

    public static List<Map<String, String>> extractFileMetadata(List<File> files) {
        List<Map<String, String>> metadataList = new ArrayList<>();
        
        for (File file : files) {
            Map<String, String> metadata = new HashMap<>();
            
            // Extract only file name and file extension
            metadata.put("fileName", file.getName());
            metadata.put("fileExtension", getFileExtension(file.getName()));
            
            metadataList.add(metadata);
        }
        
        return metadataList;
    }
    
    // Send metadata to the database
    private static void sendToDatabase(List<Map<String, String>> metadataList) {
        // TODO: Implement database connection and storage
        System.out.println("Sending " + metadataList.size() + " records to database...");
        for (Map<String, String> metadata : metadataList) {
            System.out.println("File Name: " + metadata.get("fileName") + ", File Extension: " + metadata.get("fileExtension"));
        }
    }
    
    // Get file extension from filename
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return ""; // Return empty string if no extension is found
    }
}
