// Importing the necessary libraries

import jxl.write.WriteException; // JExcel library for handling exceptions while writing to Excel
import org.opencv.core.*; // OpenCV library for handling core functionalities
import org.opencv.imgproc.Imgproc; // OpenCV library for image processing

import java.util.ArrayList; // Java utility library for handling ArrayList
import java.util.List; // Java utility library for handling List

/**
 * The CountVehicles class is responsible for detecting and classifying vehicles in a video feed,
 * and determining when a vehicle crosses the counting line or the speed line.
 * It uses the OpenCV library for image processing.
 */
public class CountVehicles {
    // Fields for storing image, contours and points for drawing lines
// The image on which the vehicle detection and counting will be performed
    private Mat image;

    // A list of contours that pass the area threshold check and are considered as potential vehicles
    public List<MatOfPoint> goodContours = new ArrayList<MatOfPoint>();

    // The minimum area a contour must have to be considered a potential vehicle
    private int areaThreshold;

    // The size threshold used for classifying the vehicles into different types (Car, Van, Lorry)
    private int vehicleSizeThreshold;

    // The two points defining the line for counting vehicles
    private Point lineCount1;
    private Point lineCount2;

    // The two points defining the line for measuring vehicle speed
    private Point lineSpeed1;
    private Point lineSpeed2;

    // An instance of CheckCrossLine class for checking if a vehicle crosses the counting line
    CheckCrossLine checkRectLine;

    // An instance of CheckCrossLine class for checking if a vehicle crosses the speed line
    CheckCrossLine checkSpeedLine;

    // A flag indicating if a vehicle is currently crossing the counting line
    boolean countingFlag = false;

    // A flag indicating if a vehicle is currently crossing the speed line
    boolean speedFlag = false;

    // A flag indicating if a vehicle has crossed the counting line
    boolean crossingLine;

    // A flag indicating if a vehicle has crossed the speed line
    boolean crossingSpeedLine;

    // The contour of the vehicle that is currently being processed
    MatOfPoint contourVehicle;

    /**
     * Constructor for the CountVehicles class.
     * Initializes the fields with the provided parameters.
     */
    public CountVehicles(int areaThreshold, int vehicleSizeThreshold, Point lineCount1, Point lineCount2, Point lineSpeed1, Point lineSpeed2, boolean crossingLine, boolean crossingSpeedLine) {
        this.areaThreshold = areaThreshold;
        this.vehicleSizeThreshold = vehicleSizeThreshold;
        this.lineCount1 = lineCount1;
        this.lineCount2 = lineCount2;
//        this.lineCount3 = lineCount3;
        this.lineSpeed1 = lineSpeed1;
        this.lineSpeed2 = lineSpeed2;
        this.crossingLine = crossingLine;
        this.crossingSpeedLine = crossingSpeedLine;
        this.checkRectLine = new CheckCrossLine(lineCount1, lineCount2);
        this.checkSpeedLine = new CheckCrossLine(lineSpeed1, lineSpeed2);
    }


    /**
     * This method finds and draws contours on the given image.
     * It uses the OpenCV library to find contours in the binary image.
     * It draws lines on the image and adds contours that have an area greater than a certain threshold to the goodContours list.
     *
     * @param image The image on which the vehicle detection and counting will be performed.
     * @param binary The binary image used for finding contours.
     * @return The image with the drawn contours and lines.
     */
    public Mat findAndDrawContours(Mat image, Mat binary) {
        // List to store the contours found in the binary image
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        // Set the class image field to the provided image
        this.image = image;

        // Find contours in the binary image
        Imgproc.findContours(binary, contours, new Mat(), Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Draw the lines for counting vehicles and measuring speed on the image
        Imgproc.line(image, lineCount1, lineCount2, new Scalar(255, 255, 255), 1);
        Imgproc.line(image, lineSpeed1, lineSpeed2, new Scalar(255, 255, 0), 1);

        // Iterate over the found contours
        for (int i = 0; i < contours.size(); i++) {
            // Get the current contour
            MatOfPoint currentContour = contours.get(i);

            // Calculate the area of the current contour
            double currentArea = Imgproc.contourArea(currentContour);

            // If the area of the current contour is greater than the area threshold, add it to the goodContours list and draw a bounding box around it
            if (currentArea > areaThreshold) {
                goodContours.add(contours.get(i));
                drawBoundingBox(currentContour);
            }
        }

        // Return the image with the drawn contours and lines
        return image;
    }

    /**
     * This method checks if a vehicle should be added to the count.
     * It iterates over the goodContours list, which contains contours that have passed the area threshold check.
     * For each contour, it creates a bounding rectangle and checks if this rectangle contains the counting line.
     * If it does, the contour is set as the current vehicle contour and the countingFlag is set to true.
     * If the countingFlag is true and the counting line was not previously crossed, the method returns true.
     * If the counting line was previously crossed, the method returns false.
     * If none of the goodContours contain the counting line, the method resets the crossingLine flag and returns false.
     *
     * @return true if a vehicle should be added to the count, false otherwise.
     */
    public boolean isVehicleToAdd() {
        for (int i = 0; i < goodContours.size(); i++) {
            Rect rectangle = Imgproc.boundingRect(goodContours.get(i));
            if (checkRectLine.rectContainLine(rectangle)) {
                contourVehicle = getGoodContours().get(i);
                countingFlag = true;
                break;
            }
        }
        if (countingFlag == true) {
            if (crossingLine == false) {
                crossingLine = true;
                return true;
            } else {

                return false;
            }
        } else {
            crossingLine = false;
            return false;
        }
    }

    /**
     * This method classifies the vehicle based on the area of its contour.
     * It uses the contour of the vehicle currently being processed.
     * If the area of the contour is less than or equal to the vehicle size threshold, the vehicle is classified as a "Car".
     * If the area of the contour is less than or equal to 1.9 times the vehicle size threshold, the vehicle is classified as a "Van".
     * Otherwise, the vehicle is classified as a "Lorry".
     *
     * @return The classification of the vehicle ("Car", "Van", or "Lorry").
     */
    public String classifier() {
        double currentArea = Imgproc.contourArea(contourVehicle);
        if (currentArea <= (double) vehicleSizeThreshold)
            return "Car";
        else if (currentArea <= 1.9 * (double) vehicleSizeThreshold)
            return "Van";
        else return "Lorry";
    }

    /**
     * This method draws a bounding box around the given contour on the image.
     * The bounding box is represented by a rectangle, which is calculated using the boundingRect method from the Imgproc class.
     * The top left and bottom right points of the rectangle are used to draw the bounding box on the image.
     * The color of the bounding box is specified by the Scalar object (255, 100, 10), which represents the color in BGR format.
     *
     * @param currentContour The contour for which the bounding box is to be drawn.
     */
    private void drawBoundingBox(MatOfPoint currentContour) {
        Rect rectangle = Imgproc.boundingRect(currentContour);
        Imgproc.rectangle(image, rectangle.tl(), rectangle.br(), new Scalar(255, 100, 10), 1);

    }

    /**
     * This method checks if a vehicle's speed should be measured.
     * It iterates over the goodContours list, which contains contours that have passed the area threshold check.
     * For each contour, it creates a bounding rectangle and checks if this rectangle contains the speed line.
     * If it does, the speedFlag is set to true.
     * If the speedFlag is true and the speed line was not previously crossed, the method returns true.
     * If the speed line was previously crossed, the method returns false.
     * If none of the goodContours contain the speed line, the method resets the crossingSpeedLine flag and returns false.
     *
     * @return true if a vehicle's speed should be measured, false otherwise.
     */

    public boolean isToSpeedMeasure() {
        for (int i = 0; i < goodContours.size(); i++) {
            Rect rectangle = Imgproc.boundingRect(goodContours.get(i));
            if (checkSpeedLine.rectContainLine(rectangle)) {
                speedFlag = true;
                break;
            }
        }
        if (speedFlag == true) {
            if (crossingSpeedLine == false) {
                crossingSpeedLine = true;
                return true;
            } else {
                return false;
            }
        } else {
            crossingSpeedLine = false;
            return false;
        }
    }

    /**
     * The isCrossingSpeedLine and isCrossingLine methods
     * return the current state of crossingSpeedLine
     * and crossingLine respectively.These methods are used to check
     * if a vehicle is currently crossing the speed line or the counting line.
     * @return The current state of the crossingSpeedLine flag.
     */
    public boolean isCrossingSpeedLine() {
        return crossingSpeedLine;
    }

    public boolean isCrossingLine() {
        return crossingLine;
    }

    public List<MatOfPoint> getGoodContours() {
        return goodContours;
    }

    /**
     * In summary, the CountVehicles class is responsible
     * for detecting and classifying vehicles in a video feed,
     * and determining when a vehicle crosses the counting line or the speed line.
     */

}