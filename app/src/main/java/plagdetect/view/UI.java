package plagdetect.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

import plagdetect.controller.FileController;
import plagdetect.controller.JPlagDetect;

import java.io.File;
import java.util.List;

public class UI extends Application {

    private final FileController fileController = new FileController();
    private final JPlagDetect jPlagDetect = new JPlagDetect();

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        // Add "Download Files" button
        Button downloadDriveButton = new Button("Download Files");
        downloadDriveButton.setOnAction(e -> downloadFilesFromDrive());

        // Add "Upload Files" button
        Button uploadFilesButton = new Button("Upload Files");
        uploadFilesButton.setOnAction(e -> uploadFilesFromSubmissions());

        // Add "View Uploaded Files" button
        Button viewFilesButton = new Button("View Uploaded Files");
        viewFilesButton.setOnAction(e -> viewUploadedFiles());

        // Add "Detect Plagiarism" button
        Button plagDetectButton = new Button("Detect Plagiarism");
        plagDetectButton.setOnAction(e -> detectPlag());

        vbox.getChildren().addAll(downloadDriveButton, uploadFilesButton, viewFilesButton, plagDetectButton);

        Scene scene = new Scene(vbox, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Plagiarism Detection App");
        primaryStage.show();
    }

    private void downloadFilesFromDrive() {
        try {
            // Step 1: Download files from Google Drive
            fileController.downloadFilesFromDrive();
            showAlert("Success", "Files downloaded successfully from Google Drive.");
        } catch (Exception e) {
            showAlert("Error", "Error downloading files: " + e.getMessage());
        }
    }

    private void uploadFilesFromSubmissions() {
        try {
            // Locate the submissions directory
            File submissionsDir = new File("src/main/resources/submissions");
            File[] filesToUpload = submissionsDir.listFiles();

            if (filesToUpload != null && filesToUpload.length > 0) {
                fileController.uploadFiles(List.of(filesToUpload));
                showAlert("Success", "Files from the submissions directory uploaded to the database successfully.");
            } else {
                showAlert("Info", "No files found in the submissions directory to upload.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error uploading files to the database: " + e.getMessage());
        }
    }

    private void viewUploadedFiles() {
        try {
            VBox fileListLayout = new VBox();
            fileListLayout.setSpacing(10);
            fileListLayout.setPadding(new Insets(10));
    
            // Add a "Delete All Files" button
            Button deleteAllButton = new Button("Delete All Files");
            deleteAllButton.setPrefWidth(150);
            deleteAllButton.setOnAction(e -> {
                try {
                    if (fileController.getUploadedFiles().isEmpty()) {
                        showAlert("Info", "No files available to delete.");
                    } else {
                        fileController.deleteAllFiles();
                        showAlert("Success", "All files deleted successfully.");
                        loadFileList(fileListLayout);
                    }
                } catch (Exception ex) {
                    showAlert("Error", "Error deleting files: " + ex.getMessage());
                }
            });
    
            // Add a "Sync Database" button
            Button syncDatabaseButton = new Button("Sync Database");
            syncDatabaseButton.setPrefWidth(150);
            syncDatabaseButton.setOnAction(e -> {
                try {
                    fileController.syncDatabaseWithDirectory();
                    showAlert("Success", "Database synchronized with the directory successfully.");
                    loadFileList(fileListLayout); // Refresh the file list after syncing
                } catch (Exception ex) {
                    showAlert("Error", "Error synchronizing database with the directory: " + ex.getMessage());
                }
            });
    
            fileListLayout.getChildren().addAll(deleteAllButton, syncDatabaseButton);
    
            // Load the file list
            loadFileList(fileListLayout);
    
            // Display the list in an alert dialog
            ScrollPane scrollPane = new ScrollPane(fileListLayout);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(600, 400);
    
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Uploaded Files");
            alert.setHeaderText("List of Uploaded Files");
            alert.getDialogPane().setContent(scrollPane);
            alert.showAndWait();
    
        } catch (Exception e) {
            showAlert("Error", "Error retrieving uploaded files: " + e.getMessage());
        }
    }

    private void loadFileList(VBox fileListLayout) {
        try {
            List<String[]> files = fileController.getUploadedFiles();
            fileListLayout.getChildren().removeIf(node -> node instanceof VBox);

            int index = 1;
            for (String[] file : files) {
                VBox fileEntry = new VBox();
                fileEntry.setSpacing(5);

                Label fileNameLabel = new Label(index++ + ". File Name: " + file[0]);
                Label filePathLabel = new Label("File Path: " + file[1]);
                Label uploadedTimeLabel = new Label("Uploaded Time: " + file[2]);

                Button deleteButton = new Button("Delete");
                deleteButton.setPrefWidth(100);
                deleteButton.setOnAction(e -> {
                    try {
                        fileController.deleteFile(file[0],file[1]);
                        showAlert("Success", "File '" + file[0] + "' deleted successfully.");
                        loadFileList(fileListLayout);
                    } catch (Exception ex) {
                        showAlert("Error", "Error deleting file: " + ex.getMessage());
                    }
                });

                fileEntry.getChildren().addAll(fileNameLabel, filePathLabel, uploadedTimeLabel, deleteButton);
                fileListLayout.getChildren().add(fileEntry);
            }

        } catch (Exception e) {
            showAlert("Error", "Error loading file list: " + e.getMessage());
        }
    }

    private void detectPlag() {
        jPlagDetect.handlePlagiarismDetection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
