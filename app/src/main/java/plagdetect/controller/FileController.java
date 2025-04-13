package plagdetect.controller;

import java.io.File;
// import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plagdetect.model.DriveDownloader;
import plagdetect.model.FileModel;

public class FileController {

    public void uploadFiles(List<File> files) throws Exception {
        List<String[]> fileData = new ArrayList<>();
        for (File file : files) {
            fileData.add(new String[]{file.getName(), file.getAbsolutePath()});
        }
        FileModel.saveFiles(fileData);
    }

    public List<String[]> getUploadedFiles() throws Exception {
        return FileModel.getUploadedFiles();
    }

    public void deleteAllFiles() throws Exception {
        // First delete all entries from the database
        FileModel.deleteAllFiles();
    
        String submissions_dir_path = "src/main/resources/submissions";
        File submissionsDir = new File(submissions_dir_path);
        
        if (submissionsDir.exists() && submissionsDir.isDirectory()) {
            // Get all immediate subdirectories
            File[] subdirectories = submissionsDir.listFiles(File::isDirectory);
            
            if (subdirectories != null) {
                for (File subdirectory : subdirectories) {
                    // Delete the entire subdirectory structure
                    deleteDirectoryRecursively(subdirectory);
                }
                System.out.println("All subdirectories deleted from submissions directory.");
            }
        } else {
            System.out.println("Submissions directory not found: " + submissions_dir_path);
        }
    }

    private boolean deleteDirectoryRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
        
        boolean result = directory.delete();
        if (!result) {
            System.out.println("Warning: Could not delete " + directory.getAbsolutePath());
        }
        return result;
    }
    public void deleteFile(String fileName, String filePath) throws Exception {
        // First delete entry from the database
        FileModel.deleteFile(fileName);
        
        // Get the file and its parent directory
        File fileToDelete = new File(filePath);
        File parentDirectory = fileToDelete.getParentFile();
        
        if (parentDirectory != null && parentDirectory.exists()) {
            // Delete all files in the directory first
            File[] files = parentDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        System.out.println("Warning: Could not delete file: " + file.getAbsolutePath());
                    }
                }
            }
            
            // Now delete the directory itself
            boolean deleted = parentDirectory.delete();
            if (!deleted) {
                throw new Exception("Failed to delete the parent directory: " + parentDirectory.getAbsolutePath());
            }
        } else {
            System.out.println("Warning: Parent directory not found for file: " + filePath);
        }
    }

    /**
     * Downloads files from Google Drive using the DriveDownloader class.
     * @throws Exception if an error occurs during the download process.
     */
    public void downloadFilesFromDrive() throws Exception {
        DriveDownloader driveDownloader = new DriveDownloader();

        // Replace this with your actual Google Drive folder link
        String folderLink = "https://drive.google.com/drive/folders/1zM8zhJlAnbvwx589GA5g079erkTyWiSx";

        // Pass the folder link to the method
        driveDownloader.downloadFilesFromDrive(folderLink);
    }

    public void syncDatabaseWithDirectory() throws Exception {
        // Get all files from the database
        List<String[]> dbFiles = FileModel.getUploadedFiles();

        // Locate the submissions directory
        File submissionsDir = new File("src/main/resources/submissions");
        if (!submissionsDir.exists() || !submissionsDir.isDirectory()) {
            throw new Exception("Submissions directory does not exist.");
        }

        // Get all files in the directory (including subdirectories)
        List<File> directoryFiles = new ArrayList<>();
        getAllFiles(submissionsDir, directoryFiles);

        // Create a list of file paths from the directory
        List<String> directoryFilePaths = new ArrayList<>();
        for (File file : directoryFiles) {
            directoryFilePaths.add(file.getAbsolutePath());
        }

        // Compare database files with directory files
        for (String[] dbFile : dbFiles) {
            String filePath = dbFile[1]; // File path from the database
            if (!directoryFilePaths.contains(filePath)) {
                // File is missing in the directory, delete it from the database
                FileModel.deleteFile(dbFile[0]);
                System.out.println("Deleted missing file from database: " + dbFile[0]);
            }
        }

        // Add new files from the directory to the database
        for (File file : directoryFiles) {
            boolean existsInDb = dbFiles.stream().anyMatch(dbFile -> dbFile[1].equals(file.getAbsolutePath()));
            if (!existsInDb) {
                // Extract the file extension
                String fileExtension = getFileExtension(file.getName());

                // File is new, add it to the database
                List<String[]> newFileData = new ArrayList<>();
                newFileData.add(new String[]{file.getName(), fileExtension}); // Use file extension instead of file path
                FileModel.saveFiles(newFileData); // Pass a List<String[]> to saveFiles
                System.out.println("Added new file to database: " + file.getName());
            }
        }
    }

    /**
     * Extracts metadata (file name and file extension) for files in the submissions directory.
     * @throws Exception if an error occurs during metadata extraction.
     */
    public void extractAndStoreMetadata() throws Exception {
        // Locate the submissions directory
        File submissionsDir = new File("src/main/resources/submissions");
        if (!submissionsDir.exists() || !submissionsDir.isDirectory()) {
            throw new Exception("Submissions directory does not exist.");
        }

        // Get all files in the directory
        List<File> files = new ArrayList<>();
        getAllFiles(submissionsDir, files);

        // Extract metadata
        List<Map<String, String>> metadataList = new ArrayList<>();
        for (File file : files) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("fileName", file.getName());
            metadata.put("fileExtension", getFileExtension(file.getName()));
            metadataList.add(metadata);
        }

        // Send metadata to the database (or print for now)
        sendMetadataToDatabase(metadataList);
    }

    private void sendMetadataToDatabase(List<Map<String, String>> metadataList) {
        System.out.println("Sending metadata to database...");
        for (Map<String, String> metadata : metadataList) {
            System.out.println("File Name: " + metadata.get("fileName") + ", File Extension: " + metadata.get("fileExtension"));
            // TODO: Implement actual database storage logic here
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return ""; // Return empty string if no extension is found
    }

    private void getAllFiles(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getAllFiles(file, fileList); // Recursive call for subdirectories
                } else {
                    fileList.add(file);
                }
            }
        }
    }
}
