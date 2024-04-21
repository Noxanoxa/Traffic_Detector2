/**
 * Importing the Mat class from the org.opencv.core package.
 * The Mat class represents an n-dimensional dense numerical single-channel or multi-channel array.
 * It is used for image processing and manipulation in OpenCV.
 */
import org.opencv.core.Mat;

/**
 * Importing the BackgroundSubtractorMOG2 class from the org.opencv.video package.
 * The BackgroundSubtractorMOG2 class is a Gaussian Mixture-based Background/Foreground Segmentation Algorithm.
 * It is used for creating a model of the background of a video for further processing.
 */
import org.opencv.video.BackgroundSubtractorMOG2;

/**
 * The MixtureOfGaussianBackground class is responsible for creating a mixture of Gaussian background model.
 */
public class MixtureOfGaussianBackground implements VideoProcessor {

    /**
     * Instance of the BackgroundSubtractorMOG2 class.
     * This class is a Gaussian Mixture-based Background/Foreground Segmentation Algorithm.
     * It was introduced in the paper "Improved adaptive Gausian mixture model for background subtraction" by Z.Zivkovic.
     * This is used to create a model of the background of the video for further processing.
     */
    private BackgroundSubtractorMOG2 mog;

    /**
     * Instance of the Mat class.
     * The Mat class represents an n-dimensional dense numerical single-channel or multi-channel array.
     * In this case, it is used to store the foreground of the video after the background has been subtracted.
     */
    private Mat foreground = new Mat();

    /**
     * The learning rate for the background subtraction algorithm.
     * This value is used to control how quickly the algorithm learns and adapts to changes in the video.
     * A lower value means the algorithm will learn slower and be less sensitive to changes.
     * A higher value means the algorithm will learn faster and be more sensitive to changes.
     */
    private double learningRate = 0.001;

    public MixtureOfGaussianBackground(double imageThreshold, int history) {

        mog = org.opencv.video.Video.createBackgroundSubtractorMOG2(history, imageThreshold, true);
        mog.setShadowValue(0);
    }

    /**
     * This method processes the input image by applying the background subtraction algorithm.
     * The algorithm is applied to the input image with the current learning rate, and the result is stored in the foreground matrix.
     * The foreground matrix, which contains the foreground of the video after the background has been subtracted, is then returned.
     *
     * @param inputImage The input image to be processed.
     * @return The foreground of the video after the background has been subtracted.
     */

    public Mat process(Mat inputImage) {

        mog.apply(inputImage, foreground, learningRate);

        return foreground;
    }

    /**
     * This method sets the threshold value for the background subtraction algorithm.
     * The threshold value is used to determine whether a pixel belongs to the background or the foreground.
     * A higher threshold value will result in more pixels being classified as background, while a lower value will result in more pixels being classified as foreground.
     * The threshold value is set for both the variable threshold and the variable threshold for new pixels in the MOG2 algorithm.
     *
     * @param imageThreshold The threshold value to be set.
     */

    public void setImageThreshold(double imageThreshold) {
        mog.setVarThreshold(imageThreshold);
        mog.setVarThresholdGen(imageThreshold);
//        System.out.println(mog.getVarInit());
//        mog.setVarInit(1);
    }

    /**
     * This method sets the history value for the background subtraction algorithm.
     * The history value is the number of last frames that affect the background model.
     * It is used by the algorithm to learn and create the background model.
     *
     * @param history The number of last frames that affect the background model.
     */

    public void setHistory(int history) {
        mog.setHistory(history);
    }
}