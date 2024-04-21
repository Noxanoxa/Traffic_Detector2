/**
 * This file contains the necessary imports for the ImageProcessor class.
 *
 * The org.opencv.core.Mat class is imported for handling matrices, which are used for image processing.
 * The java.awt.image.BufferedImage class is imported for creating and manipulating images that are loaded into memory.
 * The java.awt.image.DataBufferByte class is imported for handling the data buffer that contains the pixel values for a BufferedImage.
 */
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * The ImageProcessor class is responsible for converting a matrix to a buffered image.
 */
public class ImageProcessor {

    /**
     * This method converts a matrix to a BufferedImage.
     * It first sets the type of the BufferedImage to TYPE_BYTE_GRAY.
     * If the matrix has more than one channel, it changes the type to TYPE_3BYTE_BGR.
     * It then calculates the buffer size based on the number of channels, columns, and rows in the matrix.
     * A new byte array is created with the calculated buffer size.
     * The pixels from the matrix are then retrieved and stored in the byte array.
     * A new BufferedImage is created with the number of columns and rows from the matrix and the determined type.
     * The pixels from the byte array are then copied into the BufferedImage.
     *
     * @param matrix The matrix to be converted to a BufferedImage.
     * @return The BufferedImage created from the matrix.
     */
    public BufferedImage toBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}