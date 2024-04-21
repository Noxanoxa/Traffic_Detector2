/**
 * Importing the Mat class from the org.opencv.core package.
 * The Mat class represents an n-dimensional dense numerical single-channel or multi-channel array.
 * It is used for image processing and manipulation in OpenCV.
 */
import org.opencv.core.Mat;

/**
 * The VideoProcessor interface is responsible for processing a video.
 */
public interface VideoProcessor {
    /**
     * This method processes the input image by applying the video processing algorithm.
     * The specific implementation of the processing algorithm is defined in the classes that implement this interface.
     *
     * @param inputImage The input image to be processed.
     * @return The processed image.
     */
    Mat process(Mat inputImage);

    /**
     * This method sets the image threshold for the video processing algorithm.
     * The specific use of the image threshold is defined in the classes that implement this interface.
     *
     * @param imageThreshold The image threshold to be set.
     */
    void setImageThreshold(double imageThreshold);
    /**
     * This method sets the history value for the video processing algorithm.
     * The specific use of the history value is defined in the classes that implement this interface.
     *
     * @param history The history value to be set.
     */
    void setHistory(int history);


}