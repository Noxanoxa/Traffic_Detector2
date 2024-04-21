// Importing the necessary libraries

import jxl.write.WriteException; // JExcel library for handling exceptions while writing to Excel
import org.opencv.core.Core; // OpenCV library for handling core functionalities
import tw.edu.sju.ee.commons.nativeutils.NativeUtils; // Library for loading native libraries from JAR files

import java.io.IOException; // Java IO library for handling IO exceptions
/**
 * The Application class is responsible for starting the application.
 * It loads the OpenCV library and starts the GUI.
 */
public class Application {
    // Static initializer block to load the OpenCV library
    static {
        try {
            // Try to load the OpenCV library from the classpath (used for tests)
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            try {
                // If the library is not found in the classpath, try to load it from the JAR file (used during runtime)
                NativeUtils.loadLibraryFromJar("opencv_java310");
            } catch (IOException e1) {
                // If the library cannot be loaded, throw a RuntimeException
                throw new RuntimeException(e1);
            }
        }
    }

    /**
     * The main method of the application.
     * It creates a new GUI and initializes it.
     *
     * @param args the command-line arguments (not used)
     * @throws IOException if an I/O error occurs
     * @throws WriteException if an error occurs while writing to an Excel file
     * @throws InterruptedException if the thread is interrupted
     */
    public static void main(String[] args) throws IOException, WriteException, InterruptedException {
        // Create a new GUI
        GUI gui = new GUI();
        // Initialize the GUI
        gui.init();
    }
}