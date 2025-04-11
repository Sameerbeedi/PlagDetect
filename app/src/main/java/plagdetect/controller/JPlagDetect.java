package plagdetect.controller;

import plagdetect.model.JPlagDetectionService;
import de.jplag.JPlagResult;
import javafx.scene.control.Alert;
import java.io.File;

public class JPlagDetect {

    public void handlePlagiarismDetection() {
        try {
            String inputDir = "/home/shravan/builds/PlagDetect/app/src/main/resources/submissions";
            String outputDir = "/home/shravan/builds/PlagDetect/app/src/main/resources/results";
	    String jplagJarDir = "/home/shravan/builds/PlagDetect/app/src/main/lib/jplag.jar";
	    File jplagJar = new File(jplagJarDir);
	    File resultZip = new File(outputDir, "results.zip");
            JPlagResult result = JPlagDetectionService.runDetection(inputDir, outputDir);

            // Show result count
            //Alert alert = new Alert(Alert.AlertType.INFORMATION);
            //alert.setTitle("Plagiarism Detection");
            //alert.setHeaderText("Detection Complete");
            //alert.setContentText("Total comparisons made: " + result.getAllComparisons().size());
            //alert.showAndWait();

            JPlagDetectionService.openJPlagViewer(resultZip, jplagJar);
	    // You can store the result for later UI use if needed

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Detection Error");
            alert.setHeaderText("Failed to detect plagiarism");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}

