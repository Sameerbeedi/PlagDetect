package plagdetect.model;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FileModel {

    private static Connection getConnection() throws Exception {
        try (InputStream input = FileModel.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties props = new Properties();
            if (input == null) {
                throw new Exception("config.properties file not found.");
            }
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            return DriverManager.getConnection(url, user, password);
        }
    }

    public static void saveFiles(List<String[]> files) throws Exception {
        try (Connection conn = getConnection()) {
            // Prepare statements for checking and inserting files
            String checkQuery = "SELECT COUNT(*) FROM uploaded_files WHERE file_name = ?";
            String insertQuery = "INSERT INTO uploaded_files (file_name, format, similarity_score, flagged, similar_to) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                for (String[] file : files) {
                    // Check if the file already exists in the database
                    checkStmt.setString(1, file[0]); // file_name
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            // File already exists, skip insertion
                            System.out.println("Skipping duplicate file: " + file[0]);
                            continue;
                        }
                    }

                    // Insert the file into the database
                    insertStmt.setString(1, file[0]); // file_name
                    insertStmt.setString(2, file[1]); // format
                    insertStmt.setFloat(3, 0.0f); // similarity_score (default 0.0)
                    insertStmt.setBoolean(4, false); // flagged (default false)
                    insertStmt.setString(5, null); // similar_to (default null)
                    insertStmt.addBatch();
                }

                // Execute the batch insert
                insertStmt.executeBatch();
            }
        }
    }

    public static List<String[]> getUploadedFiles() throws Exception {
        List<String[]> files = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT file_name, format, similarity_score, flagged, similar_to FROM uploaded_files");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                files.add(new String[]{
                    rs.getString("file_name"),
                    rs.getString("format"),
                    String.valueOf(rs.getFloat("similarity_score")),
                    String.valueOf(rs.getBoolean("flagged")),
                    rs.getString("similar_to")
                });
            }
        }
        return files;
    }

    public static void deleteAllFiles() throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM uploaded_files")) {
            stmt.executeUpdate();
        }
    }

    public static void deleteFile(String fileName) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM uploaded_files WHERE file_name = ?")) {
            stmt.setString(1, fileName);
            stmt.executeUpdate();
        }
    }

    public static void updateSimilarityScore(String fileName, float similarityScore, boolean flagged, String similarTo) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE uploaded_files SET similarity_score = ?, flagged = ?, similar_to = ? WHERE file_name = ?")) {
            stmt.setFloat(1, similarityScore);
            stmt.setBoolean(2, flagged);
            stmt.setString(3, similarTo);
            stmt.setString(4, fileName);
            stmt.executeUpdate();
        }
    }
}