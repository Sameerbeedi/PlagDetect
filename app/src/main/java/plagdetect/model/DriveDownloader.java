package plagdetect.model;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

public class DriveDownloader {

    private static final String APPLICATION_NAME = "PlagDetect";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    /**
     * Initializes the Google Drive API client using a Service Account.
     * @return Drive service instance.
     * @throws Exception if an error occurs during initialization.
     */
    private Drive getDriveService() throws Exception {
        // Load service account credentials using getResourceAsStream
        GoogleCredentials credentials;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("credentials.json")) {
            if (in == null) {
                throw new Exception("Resource not found: credentials.json");
            }
            credentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);
        }

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void downloadFilesFromDrive(String folderLink) {
        try {
            String folderId = extractFolderId(folderLink);
            Drive driveService = getDriveService();

            // List all files in the specified folder
            Drive.Files.List request = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("files(id, name)");
            List<com.google.api.services.drive.model.File> files = request.execute().getFiles();

            if (files == null || files.isEmpty()) {
                System.out.println("No files found in the folder.");
                return;
            }

            String baseDownloadPath = "src/main/resources/submissions";
            java.io.File baseDir = new java.io.File(baseDownloadPath);
            if (!baseDir.exists()) {
                baseDir.mkdirs();
                System.out.println("Base directory created: " + baseDir.getAbsolutePath());
            }

            for (com.google.api.services.drive.model.File file : files) {
                String fileId = file.getId();
                String fileName = file.getName();
                String fileBaseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;

                // Create a subdirectory with the name of the file (excluding extension)
                java.io.File subDir = new java.io.File(baseDownloadPath, fileBaseName);
                if (!subDir.exists()) {
                    subDir.mkdirs();
                    System.out.println("Subdirectory created: " + subDir.getAbsolutePath());
                }

                System.out.println("Downloading file: " + fileName);
                try (InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();
                     java.io.FileOutputStream outputStream = new java.io.FileOutputStream(new java.io.File(subDir, fileName))) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    System.out.println("Downloaded successfully: " + new java.io.File(subDir, fileName).getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to download files: " + e.getMessage());
        }
    }

    private String extractFolderId(String folderLink) {
        if (folderLink.contains("/folders/")) {
            String[] parts = folderLink.split("/folders/");
            if (parts.length > 1) {
                String[] idParts = parts[1].split("\\?");
                return idParts[0];
            }
        }
        throw new IllegalArgumentException("Invalid Google Drive folder link: " + folderLink);
    }
}

