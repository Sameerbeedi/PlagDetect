package plagdetect.model;

import de.jplag.JPlag;
import de.jplag.JPlagResult;
import de.jplag.options.JPlagOptions;
import de.jplag.java.JavaLanguage;
import de.jplag.reporting.reportobject.ReportObjectFactory;

import java.io.File;
import java.util.Set;

public class JPlagDetectionService {

    public static JPlagResult runDetection(String submissionDirPath, String resultDirPath) throws Exception {
        File submissionDir = new File(submissionDirPath);
        File resultDir = new File(resultDirPath);

        if (!submissionDir.exists() || !submissionDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid submission directory: " + submissionDirPath);
        }

        JPlagOptions options = new JPlagOptions(new JavaLanguage(), Set.of(submissionDir), Set.of(resultDir));

        JPlagResult result = JPlag.run(options);

        // Save report
        File resultZip = new File(resultDir, "results.zip");
        ReportObjectFactory factory = new ReportObjectFactory(resultZip);
        factory.createAndSaveReport(result);

        return result;
    }

    public static void openJPlagViewer(File resultZip, File jplagJar) throws Exception {
    if (!jplagJar.exists()) {
        throw new IllegalArgumentException("jplag.jar not found at: " + jplagJar.getAbsolutePath());
    }

    // Build the command: java -jar jplag.jar path/to/result.zip
    ProcessBuilder processBuilder = new ProcessBuilder(
        "java", "-jar", jplagJar.getAbsolutePath(), resultZip.getAbsolutePath()
    );

    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    // Optional: Open the default browser to localhost:5000 after a short delay
    new Thread(() -> {
        try {
            Thread.sleep(3000); // wait for the server to boot up
            java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:1996"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

}
