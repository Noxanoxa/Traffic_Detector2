// Importing the necessary libraries

import org.opencv.core.Point; // OpenCV library for handling points in 2D space
import org.opencv.core.Rect; // OpenCV library for handling rectangles in 2D space
/**
 * The CheckCrossLine class is responsible for checking if a line crosses a rectangle.
 * It uses the mathematical equation of a line and checks if the line intersects with the rectangle.
 */
public class CheckCrossLine {

    // Coordinates of the line
    public int lAx;
    public int lAy;
    public int lBx;
    public int lBy;

    // Coefficients of the line equation y = ax + b
    public double a;
    public double b;

    // Points representing the line
    Point l1;
    Point l2;

    /**
     * Constructor for the CheckCrossLine class.
     * It initializes the points of the line and calculates the coordinates and coefficients of the line.
     *
     * @param l1 the first point of the line
     * @param l2 the second point of the line
     */
    public CheckCrossLine(Point l1, Point l2) {
        this.l1 = l1;
        this.l2 = l2;
        // Calculate the coordinates of the line
        this.lAx = (int) (Math.min(l1.x, l2.x));
        this.lAy = (int) (Math.min(l1.y, l2.y));
        this.lBx = (int) (Math.max(l1.x, l2.x));
        this.lBy = (int) (Math.max(l1.y, l2.y));
    }

    /**
     * This method checks if the line defined in the class crosses the given rectangle.
     * It uses the mathematical equation of a line and checks if the line intersects with the rectangle.
     *
     * @param rect The rectangle to check for intersection with the line.
     * @return true if the line crosses the rectangle, false otherwise.
     */
    public boolean rectContainLine(Rect rect) {
        // Calculate the center point of the rectangle
        int PrA = (int) ((rect.tl().x + rect.br().x) / 2);      // X-coordinate of the center point
        int PrB = (int) ((rect.tl().y + rect.br().y) / 2);      // Y-coordinate of the center point

        // Calculate the top and bottom Y-coordinates of the rectangle
        int pktCy = (int) (rect.tl().y); // Top Y-coordinate
        int pktDy = (int) (rect.br().y); // Bottom Y-coordinate

        // Calculate the left and right X-coordinates of the rectangle
        int pktEx = (int) (rect.tl().x); // Left X-coordinate
        int pktFx = (int) (rect.br().x); // Right X-coordinate

        // Check if the line is not a point
        if (lBx != lAx && lBy != lAy) {
            // Calculate the coefficients of the line equation y = ax + b
            this.a = (l2.y - l1.y) / (l2.x - l1.x);
            this.b = l1.y - a * l1.x;

            // Calculate the intersection points of the line with the vertical and horizontal center lines of the rectangle
            int crax = PrA;
            double cray = (a * PrA + b);
            double crbx = (PrB - b) / a;
            int crby = PrB;

            // Check if the line crosses the rectangle
            if ((lAx <= crax) && (lBx >= crax) &&
                    (lAy <= cray) && (lBy >= cray) &&
                    (pktCy <= cray) && (pktDy >= cray))
                return true;
            else if ((lAx <= crbx) && (lBx >= crbx) &&
                    (lAy <= crby) && (lBy >= crby) &&
                    (pktEx <= crbx) && (pktFx >= crbx))
                return true;
            else
                return false;
        } else
            return false;
    }
}