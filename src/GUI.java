// Importing the necessary libraries

import com.opencsv.CSVWriter; // Library for writing to CSV files
import jxl.Cell; // JExcel library for handling Excel cells
import jxl.Workbook; // JExcel library for handling Excel workbooks
import jxl.write.Number; // JExcel library for handling numeric data in Excel
import jxl.write.WritableSheet; // JExcel library for handling writable Excel sheets
import jxl.write.WritableWorkbook; // JExcel library for handling writable Excel workbooks
import jxl.write.WriteException; // JExcel library for handling exceptions while writing to Excel

import org.opencv.core.*; // OpenCV library for handling core functionalities
import org.opencv.core.Point; // OpenCV library for handling points in 2D space
import org.opencv.imgproc.Imgproc; // OpenCV library for handling image processing functionalities
import org.opencv.videoio.VideoCapture; // OpenCV library for capturing video
import org.opencv.videoio.VideoWriter; // OpenCV library for writing video
import org.opencv.videoio.Videoio; // OpenCV library for handling video I/O operations

import javax.swing.*; // Swing library for creating GUI
import javax.swing.filechooser.FileNameExtensionFilter; // Swing library for filtering file names based on their extensions
import java.awt.*; // AWT library for creating GUI
import java.awt.event.*; // AWT library for handling events
import java.io.File; // Java IO library for handling files
import java.io.FileWriter; // Java IO library for writing to files
import java.io.IOException; // Java IO library for handling IO exceptions
import java.text.NumberFormat; // Java Text library for formatting numbers
import java.util.ArrayList; // Java Util library for handling ArrayLists
import java.util.HashMap; // Java Util library for handling HashMaps

import static org.opencv.imgproc.Imgproc.resize; // OpenCV library for resizing images

/**
 * This class is responsible for analyzing traffic in a video.
 * It counts the number of vehicles, measures their speed, and writes the results to a file.
 * The user can draw lines on the video to specify where the vehicles should be counted and where their speed should be measured.
 * The results can be saved in either XLS or CSV format.
 * The user can also choose to save the processed video.
 * The GUI class contains all the GUI components and the logic for the traffic detector application.
 * It includes various buttons for controlling the application, fields for displaying the results, and settings for configuring the detection process.
 * It also contains the logic for processing the video, counting the vehicles, measuring their speed, and writing the results to a file.
 */
public class GUI {
    // GUI components
    private JLabel imageView; // The label for displaying the video
    private JFrame frame; // The main frame of the application
    private JFrame frameBGS; // The frame for displaying the background subtraction view
    private JLabel BGSview; // The label for displaying the background subtraction view

    // Buttons for controlling the application
    private JButton playPauseButton; // The button for playing and pausing the video
    JButton loadButton; // The button for loading a video
    private JButton saveButton; // The button for saving the results
    private JButton resetButton; // The button for resetting the application
    private JButton countingLineButton; // The button for selecting the counting line
    private JButton speedLineButton; // The button for selecting the speed line

    // Flags for controlling the application
    private volatile boolean isPaused = true; // Whether the video is paused
    private boolean crossingLine = false; // Whether a vehicle is crossing the counting line
    private boolean crossingSpeedLine = false; // Whether a vehicle is crossing the speed line

    // Settings for the detection process
    private int areaThreshold = 1700; // The area threshold for detecting vehicles
    private double imageThreshold = 20; // The image threshold for the background subtraction
    private int history = 1500; // The history for the background subtraction
    private int vehicleSizeThreshold = 20000; // The vehicle size threshold for classifying the vehicles

    // The video capture for reading the video
    private VideoCapture capture;
    // The current image from the video
    private Mat currentImage = new Mat();
    // The video processor for processing the video
    private VideoProcessor videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
    // The image processor for processing the images
    private ImageProcessor imageProcessor = new ImageProcessor();
    // The foreground image from the background subtraction
    private Mat foregroundImage;

    // The points for the counting line
    private Point lineCount1; // The first point of the counting line
    private volatile Point lineCount2; // The second point of the counting line
    // The points for the speed line
    private Point lineSpeed1; // The first point of the speed line
    private volatile Point lineSpeed2; // The second point of the speed line
    // The counter for the vehicles
    private int counter = 0;
    // The last time stamp for measuring the speed
    private int lastTSM = 0;
    // The speeds of the vehicles
    private HashMap<Integer, Integer> speed = new HashMap<Integer, Integer>();

    // The distance between the counting line and the speed line
    private double distanceCS = 6.0;
    // The frames per second of the video
    private double videoFPS;
    // The maximum frames per second for displaying the video
    private int maxFPS;
    // The current frame of the video
    private int whichFrame;
    // The field for setting the distance between the counting line and the speed line
    private JSpinner distanceBLfield;

    // The file for saving the results in XLS format
    private File fileToSaveXLS;
    // The workbook for writing the XLS file
    private WritableWorkbook workbook;
    // The sheet for writing the XLS file
    private WritableSheet sheet;
    // The label for writing the XLS file
    private jxl.write.Label label;
    // The number for writing the XLS file
    private Number number;

    // The CSV writer for writing the CSV file
    private CSVWriter CSVwriter;
    // The list for storing the data for the CSV file
    private ArrayList<String[]> ListCSV = new ArrayList<>();
    // The file writer for writing the CSV file
    private FileWriter filetoSaveCSV;

    // The radio buttons for selecting the format for saving the results
    private JRadioButton xlsButton; // The radio button for selecting the XLS format
    private JRadioButton csvButton; // The radio button for selecting the CSV format
    // The flags for the formats for saving the results
    private static final String xlsWriteResults = "XLS";
    private static final String csvWriteResults = "CSV";
    // The flag for the selected format for saving the results
    private String writeFlag = xlsWriteResults;
    // Whether the XLS format is selected
    private boolean isExcelToWrite = true;
    // Whether the results have been written
    private boolean isWritten = false;

    // The paths for the video and the results
    private volatile String videoPath; // The path of the video
    private volatile String savePath; // The path for saving the results

    // The fields for displaying the results
    private JFormattedTextField carsAmountField; // The field for displaying the number of cars
    private JFormattedTextField carsSpeedField; // The field for displaying the speed of cars
    private JFormattedTextField vansAmountField; // The field for displaying the number of vans
    private JFormattedTextField vansSpeedField; // The field for displaying the speed of vans
    private JFormattedTextField lorriesAmountField; // The field for displaying the number of lorries
    private JFormattedTextField lorriesSpeedField; // The field for displaying the speed of lorries

    // The counters for the vehicles
    private int cars = 0; // The counter for cars
    private int vans = 0; // The counter for vans
    private int lorries = 0; // The counter for lorries

    // The sums of the speeds of the vehicles
    private double sumSpeedCar = 0; // The sum of the speeds of cars
    private double sumSpeedVan = 0; // The sum of the speeds of vans
    private double sumSpeedLorry = 0; // The sum of the speeds of lorries

    // The divisors for calculating the average speeds of the vehicles
    private int divisorCar = 1; // The divisor for cars
    private int divisorVan = 1; // The divisor for vans
    private int divisorLorry = 1; // The divisor for lorries

    // The radio buttons for selecting whether to save the video
    private JRadioButton onButton; // The radio button for selecting to save the video
    private JRadioButton offButton; // The radio button for selecting not to save the video
    // The flags for whether to save the video
    private static final String onSaveVideo = "On";
    private static final String offSaveVideo = "Off";
    // The flag for whether to save the video
    private String saveFlag = offSaveVideo;
    // Whether to save the video
    private boolean isToSave = false;
    // The video writer for saving the video
    private VideoWriter videoWriter;

    // The flags for whether the mouse listeners are active
    private boolean mouseListenertIsActive; // Whether the mouse listener for the counting line is active
    private boolean mouseListenertIsActive2; // Whether the mouse listener for the speed line is active
    // Whether the drawing of the lines has started
    private boolean startDraw;
    // The copied image for drawing the lines
    private Mat copiedImage;

    // Whether to break the loop for processing the video
    private volatile boolean loopBreaker = false;

    // The button for displaying the background subtraction view
    private JButton BGSButton;
    // The field for setting the image threshold
    private JSpinner imgThresholdField;
    // Whether the background subtraction view is displayed
    private volatile boolean isBGSview = false;
    // The image for the background subtraction view
    private Mat ImageBGS = new Mat();

    // The field for setting the history
    private JSpinner videoHistoryField;

    // The field for displaying the current time
    private JFormattedTextField currentTimeField;
    // The current time in seconds
    private double timeInSec;
    // The current time in minutes and seconds
    private int minutes = 1;
    private int second = 0;

    // The button for toggling the real time processing
    private JButton realTimeButton;
    // Whether the real time processing is on
    private volatile boolean isProcessInRealTime = false;
    // The start time for the real time processing
    private long startTime;
    // The duration of one frame for the real time processing
    private long oneFrameDuration;

    // The clone of the foreground image
    private Mat foregroundClone;

    /**
     * This method initializes the GUI and starts the main loop.
     * It sets the system look and feel, initializes the GUI, and waits for the user to select a video and a save path.
     * Once the user has drawn the counting and speed lines, it starts the main loop.
     * The main loop reads frames from the video, processes them, and updates the GUI.
     * It also writes the results to a file.
     */

    public void init() throws IOException, WriteException, InterruptedException {
        setSystemLookAndFeel();
        initGUI();

        while (true) {
            if (videoPath != null && savePath != null) {
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);

                resetButton.setEnabled(true);
                break;
            }
        }

        while (true) {
            if (lineSpeed2 != null && lineCount2 != null) {

                playPauseButton.setEnabled(true);
                if (saveFlag.equals(onSaveVideo)) {
                    videoWriter = new VideoWriter(savePath + "\\Video.avi", VideoWriter.fourcc('P', 'I', 'M', '1'), videoFPS, new Size(640, 360));
                }
                onButton.setEnabled(false);
                offButton.setEnabled(false);


                String xlsSavePath = savePath + "\\Results.xls";
                fileToSaveXLS = new File(xlsSavePath);
                try {
                    writeToExel(fileToSaveXLS);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }

                if (!isExcelToWrite) {
                    String csvSavePath = savePath + "\\Results.csv";
                    try {
                        filetoSaveCSV = new FileWriter(csvSavePath);
                        writeToCSV(filetoSaveCSV);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);

                break;
            }
        }


        Thread mainLoop = new Thread(new Loop());
        mainLoop.start();
    }

    /**
     * This method initializes the GUI.
     * It creates the JFrame, sets up the video view, and adds the necessary buttons and fields.
     */
    public void initGUI() {
        frame = createJFrame("Traffic Detector");

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        playPauseButton.setEnabled(false);
        countingLineButton.setEnabled(false);
        speedLineButton.setEnabled(false);
        distanceBLfield.setEnabled(false);
        resetButton.setEnabled(false);


    }


    /**
     * This class represents the main loop.
     * It reads frames from the video, processes them, and updates the GUI.
     * It also writes the results to a file.
     */
    public class Loop implements Runnable {

        @Override
        public void run() {

            maxWaitingFPS();
            videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
            if (capture.isOpened()) {
                while (true) {
                    if (!isPaused) {
                        capture.read(currentImage);
                        if (!currentImage.empty()) {
                            resize(currentImage, currentImage, new Size(640, 360));
                            foregroundImage = currentImage.clone();
                            foregroundImage = videoProcessor.process(foregroundImage);

                            foregroundClone = foregroundImage.clone();
                            Imgproc.bilateralFilter(foregroundClone, foregroundImage, 2, 1600, 400);

                            if (isBGSview) {
                                resize(foregroundImage, ImageBGS, new Size(430, 240));
                                BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(ImageBGS)));
                            }

                            CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount1, lineCount2, lineSpeed1, lineSpeed2, crossingLine, crossingSpeedLine);
                            countVehicles.findAndDrawContours(currentImage, foregroundImage);

                            try {
                                count(countVehicles);
                                speedMeasure(countVehicles);
                            } catch (WriteException e) {
                                e.printStackTrace();
                            }


                            videoRealTime();

                            saveVideo();

                            if (isProcessInRealTime) {
                                long time = System.currentTimeMillis() - startTime;
                                if (time < oneFrameDuration) {
                                    try {
                                        Thread.sleep(oneFrameDuration - time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            updateView(currentImage);
                            startTime = System.currentTimeMillis();

                            if (loopBreaker)
                                break;

                        } else {
                            if (isToSave)
                                videoWriter.release();

                            if (!isWritten) {
                                try {
                                    workbook.write();
                                    workbook.close();
                                } catch (IOException | WriteException e) {
                                    e.printStackTrace();
                                }

                                if (!isExcelToWrite) {
                                    try {
                                        CSVwriter.writeAll(ListCSV);
                                        CSVwriter.close();
                                        new File(savePath + "\\Results.xls").delete();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                isWritten = true;
                            }

                            playPauseButton.setEnabled(false);

                            saveButton.setEnabled(true);
                            loadButton.setEnabled(true);

                            playPauseButton.setText("Play");

                            minutes = 1;
                            second = 0;
                            whichFrame = 0;
//                            System.out.println("The video has finished!");
                            break;
                        }
                    }
                }
            }
        }
    }


    private void saveVideo() {
        if (isToSave)
            videoWriter.write(currentImage);
    }

    /**
     * This method is responsible for counting the vehicles in the video.
     * It increments the counter for each vehicle detected and classifies the vehicle type.
     * The vehicle type can be a Car, Van, or Lorry.
     * The method also updates the corresponding fields in the GUI and writes the results to an Excel sheet.
     * It also checks if a vehicle is crossing the line and updates the 'crossingLine' variable accordingly.
     *
     * @param countVehicles an instance of the CountVehicles class which is used to detect and classify vehicles.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    public synchronized void count(CountVehicles countVehicles) throws WriteException {
        // Check if a new vehicle is detected
        if (countVehicles.isVehicleToAdd()) {
            // Increment the total vehicle counter and the last Time-Space Measurement (TSM) counter
            counter++;
            lastTSM++;
            // Initialize the speed of the new vehicle to 0
            speed.put(lastTSM, 0);
            // Classify the type of the new vehicle
            String vehicleType = countVehicles.classifier();
            // Update the count of the vehicle type and the corresponding field in the GUI
            switch (vehicleType) {
                case "Car":
                    cars++;
                    carsAmountField.setValue(cars);
                    break;
                case "Van":
                    vans++;
                    vansAmountField.setValue(vans);
                    break;
                case "Lorry":
                    lorries++;
                    lorriesAmountField.setValue(lorries);
                    break;
            }
            // Write the vehicle count and type to the Excel sheet
            addNumberInteger(sheet, 0, counter, counter);
            addLabel(sheet, 1, counter, vehicleType);
        }
        // Check if a vehicle is crossing the line
        crossingLine = countVehicles.isCrossingLine();
    }


    /**
     * This method measures the speed of vehicles in the video.
     * It checks if the speed map is not empty and gets the first Time-Space Measurement (TSM).
     * If a vehicle is ready for speed measurement, it increments the speed for each TSM in the map.
     * It then calculates the current speed of the vehicle and gets the vehicle type from the Excel sheet.
     * Depending on the vehicle type, it updates the total speed and average speed for that type of vehicle.
     * It also writes the current speed and video time to the Excel sheet.
     * If the vehicle is not ready for speed measurement, it increments the speed for each TSM in the map.
     * If the speed exceeds the maximum FPS, it removes the TSM from the map and decrements the count for that type of vehicle.
     * Finally, it checks if a vehicle is crossing the speed line and updates the 'crossingSpeedLine' variable accordingly.
     *
     * @param countVehicles an instance of the CountVehicles class which is used to detect and classify vehicles.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    public synchronized void speedMeasure(CountVehicles countVehicles) throws WriteException {
        // Check if the speed map is not empty
        if (!speed.isEmpty()) {
            // Get the first Time-Space Measurement (TSM)
            int firstTSM = speed.entrySet().iterator().next().getKey();
            // Check if a vehicle is ready for speed measurement
            if (countVehicles.isToSpeedMeasure()) {
                // Increment the speed for each TSM in the map
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        speed.put(i, (speed.get(i) + 1));
                    }
                }

                // Calculate the current speed of the vehicle
                double currentSpeed = computeSpeed(speed.get(firstTSM));
                // Get the vehicle type from the Excel sheet
                Cell cell = sheet.getWritableCell(1, firstTSM);
                String carType = cell.getContents();
                // Update the total speed and average speed for the vehicle type
                switch (carType) {
                    case "Car":
                        sumSpeedCar = sumSpeedCar + currentSpeed;
                        double avgspeed1 = sumSpeedCar / divisorCar;
                        divisorCar++;
                        carsSpeedField.setValue(avgspeed1);
                        break;
                    case "Van":
                        sumSpeedVan = sumSpeedVan + currentSpeed;
                        double avgspeed2 = sumSpeedVan / divisorVan;
                        divisorVan++;
                        vansSpeedField.setValue(avgspeed2);
                        break;
                    case "Lorry":
                        sumSpeedLorry = sumSpeedLorry + currentSpeed;
                        double avgspeed3 = sumSpeedLorry / divisorLorry;
                        divisorLorry++;
                        lorriesSpeedField.setValue(avgspeed3);
                        break;
                }

                // Write the current speed and video time to the Excel sheet
                addNumberDouble(sheet, 2, firstTSM, currentSpeed);
                addNumberDouble(sheet, 3, firstTSM, timeInSec);

                // If the results are to be written to a CSV file, add the data to the CSV list
                if (!isExcelToWrite) {
                    ListCSV.add((firstTSM + "#" + carType + "#" + currentSpeed + "#" + timeInSec).split("#"));
                }

                // Remove the first TSM from the map
                speed.remove(firstTSM);

            } else {
                // If a vehicle is not ready for speed measurement, increment the speed for each TSM in the map
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        int currentFPS = speed.get(i);
                        speed.put(i, (currentFPS + 1));
                        // If the speed exceeds the maximum FPS, remove the TSM from the map and decrement the count for that type of vehicle
                        if (currentFPS > maxFPS) {
                            speed.remove(i);

                            Cell cell = sheet.getWritableCell(1, i);
                            String carType = cell.getContents();
                            switch (carType) {
                                case "Car":
                                    cars--;
                                    carsAmountField.setValue(cars);
                                    break;
                                case "Van":
                                    vans--;
                                    vansAmountField.setValue(vans);
                                    break;
                                case "Lorry":
                                    lorries--;
                                    lorriesAmountField.setValue(lorries);
                                    break;
                            }

                        }
                    }
                }
            }
        }
        // Check if a vehicle is crossing the speed line
        crossingSpeedLine = countVehicles.isCrossingSpeedLine();
    }

    /**
     * This method creates a JFrame with the specified window name and sets up the GUI.
     * It sets the layout of the frame to GridBagLayout and calls various methods to add components to the frame.
     * These components include video setup, play/pause button, save video setup, write type setup, load file button, save file button,
     * information fields for cars, vans, and lorries, buttons to select counting and speed lines, fields to setup distance between lines,
     * image threshold, video history, area threshold, vehicle size threshold, BGS visibility, current time, and real time setup.
     * It also sets the default close operation for the frame to EXIT_ON_CLOSE.
     *
     * @param windowName the name to be set for the JFrame window.
     * @return the created JFrame with all the components added and set up.
     */
    private JFrame createJFrame(String windowName) {
        frame = new JFrame(windowName);
        frame.setLayout(new GridBagLayout());

        setupVideo(frame);

        reset(frame);
        playPause(frame);
        setupSaveVideo(frame);
        setupWriteType(frame);

        loadFile(frame);
        saveFile(frame);

        infoCars(frame);
        infoVans(frame);
        infoLorries(frame);

        selectCountingLine(frame);
        selectSpeedLine(frame);
        setupDistanceBetweenLines(frame);

        setupImageThreshold(frame);
        setupVideoHistory(frame);
        setupAreaThreshold(frame);
        setupVehicleSizeThreshold(frame);

        setupBGSvisibility(frame);
        currentTime(frame);
        setupRealTime(frame);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }

    /**
     * This method sets up the video display in the GUI.
     * It creates a JLabel for the video display and adds it to the JFrame.
     * The method also creates a GridBagConstraints object to specify the location and size of the video display in the GUI.
     * It then creates a blank image and displays it in the JLabel.
     *
     * @param frame the JFrame to which the video display is added.
     */
    private void setupVideo(JFrame frame) {
        // Create a JLabel for the video display
        imageView = new JLabel();

        // Create a GridBagConstraints object to specify the location and size of the video display in the GUI
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 6;
        c.gridheight = 9;

        // Add the JLabel to the JFrame
        frame.add(imageView, c);

        // Create a blank image
        Mat localImage = new Mat(new Size(640, 360), CvType.CV_8UC3, new Scalar(255, 255, 255));
        // Resize the image to the size of the video display
        resize(localImage, localImage, new Size(640, 360));
        // Display the image in the JLabel
        updateView(localImage);
    }

    /**
     * This method sets up the play/pause button in the GUI.
     * It creates a JButton with the text "Start" and sets its properties.
     * The method also adds an ActionListener to the button to handle the play/pause functionality.
     * When the button is clicked, it checks if the video is currently paused.
     * If the video is not paused, it pauses the video, changes the button text to "Continue", and enables/disables other buttons accordingly.
     * If the video is paused, it starts the video, changes the button text to "Pause", and enables/disables other buttons accordingly.
     * It also calls the maxWaitingFPS() method when the video is started.
     * Finally, it adds the button to the JFrame using a GridBagConstraints object to specify its location and size.
     *
     * @param frame the JFrame to which the play/pause button is added.
     */
    private void playPause(JFrame frame) {

        playPauseButton = new JButton("Start");
        playPauseButton.setPreferredSize(new Dimension(100, 40));
        playPauseButton.setFont(new Font("defaut", Font.BOLD, 15));
        playPauseButton.setBackground(Color.YELLOW);
        playPauseButton.addActionListener(event -> {
            if (!isPaused) {
                isPaused = true;
                playPauseButton.setText("Countinue");

                loadButton.setEnabled(true);
                saveButton.setEnabled(true);

                onButton.setEnabled(false);
                offButton.setEnabled(false);

                countingLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);
                speedLineButton.setEnabled(true);

                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);

            } else {
                isPaused = false;
                playPauseButton.setText("Pause");

                maxWaitingFPS();

                loadButton.setEnabled(false);
                saveButton.setEnabled(false);

                onButton.setEnabled(false);
                offButton.setEnabled(false);

                countingLineButton.setEnabled(false);
                distanceBLfield.setEnabled(false);
                speedLineButton.setEnabled(false);

                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);
                frame.pack();
            }
        });
        playPauseButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 10, 15, 10);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;

        frame.add(playPauseButton, c);
    }

    /**
     * This method sets up the file loading functionality in the GUI.
     * It creates a JTextField to display the selected file path and a JButton to open the file chooser.
     * The method also creates a JFileChooser with a filter for video files and sets its default directory to the user's desktop.
     * An ActionListener is added to the JButton to handle the file selection.
     * When the button is clicked, it opens the file chooser and waits for the user to select a file.
     * If a file is selected, it reads the file path, displays it in the JTextField, and opens the video using the VideoCapture class.
     * It also reads the first frame of the video, resizes it to fit the display, and updates the video view in the GUI.
     * Finally, it adds the JButton and JTextField to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the file loading components are added.
     */
    private void loadFile(JFrame frame) {

        JTextField field = new JTextField();
        field.setText(" ");
        field.setEditable(false);

        loadButton = new JButton("Open video", createImageIcon("resources/open.png"));

        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Video Files", "avi", "mp4", "mpg", "mov");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fc.setAcceptAllFileFilterUsed(false);

        loadButton.addActionListener(event -> {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                videoPath = file.getPath();
                field.setText(videoPath);
                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(640, 360));
                updateView(currentImage);

            }
        });
        loadButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        frame.add(loadButton, c);

        c.insets = new Insets(0, 0, 0, 10);
        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 3;
        frame.add(field, c);
    }

    /**
     * This method sets up the file saving functionality in the GUI.
     * It creates a JTextField to display the selected save path and a JButton to open the directory chooser.
     * The method also creates a JFileChooser with the directory selection mode and sets its default directory to the user's desktop.
     * An ActionListener is added to the JButton to handle the directory selection.
     * When the button is clicked, it opens the directory chooser and waits for the user to select a directory.
     * If a directory is selected, it reads the directory path, displays it in the JTextField, and sets the save path.
     * Finally, it adds the JButton and JTextField to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the file saving components are added.
     */
    private void saveFile(JFrame frame) {

        JTextField field = new JTextField();
        field.setText(" ");
        field.setPreferredSize(new Dimension(440, 20));
        field.setEditable(false);

        saveButton = new JButton("Save file", createImageIcon("resources/diskette.png"));

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fc.setAcceptAllFileFilterUsed(false);

        saveButton.addActionListener(event -> {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();

                savePath = file.getPath();
                field.setText(savePath);

            }
        });
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.insets = new Insets(0, 5, 0, 5);
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        frame.add(saveButton, c);

        c.insets = new Insets(0, 0, 0, 10);
        c.gridx = 4;
        c.gridy = 1;
        c.gridwidth = 3;
        frame.add(field, c);
    }


    /**
     * This method sets up the information display for cars in the GUI.
     * It creates JLabels for the quantity and average speed of cars, and sets their properties.
     * The method also creates JFormattedTextFields for the car amount and speed, and sets their properties.
     * The car amount field is set to red and the speed field is set to green.
     * Both fields are set to non-editable and their values are initialized to 0.
     * Finally, it adds the JLabels and JFormattedTextFields to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the car information display components are added.
     */
    private void infoCars(JFrame frame) {
        // Create JLabels for the quantity and average speed of cars
        JLabel quantityLabel = new JLabel("Quantity", JLabel.RIGHT);
        quantityLabel.setFont(new Font("defaut", Font.BOLD, 12));

        JLabel averageLabel = new JLabel("speed [km/h]", JLabel.RIGHT);
        averageLabel.setFont(new Font("defaut", Font.BOLD, 12));

        // Create a JLabel for the car label
        JLabel carsLabel = new JLabel("Cars", JLabel.CENTER);
        carsLabel.setFont(new Font("defaut", Font.BOLD, 12));

        // Create a JFormattedTextField for the car amount
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        carsAmountField = new JFormattedTextField(numberFormat);
        carsAmountField.setValue(Integer.valueOf(0));
        carsAmountField.setBackground(Color.RED);
        carsAmountField.setEditable(false);
        carsAmountField.setPreferredSize(new Dimension(50, 20));
        carsAmountField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a JFormattedTextField for the car speed
        carsSpeedField = new JFormattedTextField(numberFormat);
        carsSpeedField.setValue(Integer.valueOf(0));
        carsSpeedField.setBackground(Color.GREEN);
        carsSpeedField.setEditable(false);
        carsSpeedField.setPreferredSize(new Dimension(50, 20));
        carsSpeedField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a GridBagConstraints object to specify the location and size of the car information display in the GUI
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 12;
        c.gridwidth = 1;
        c.insets = new Insets(0, 70, 5, 5);
        frame.add(quantityLabel, c);

        c.gridy = 13;
        frame.add(averageLabel, c);

        c.insets = new Insets(0, 0, 5, 5);
        c.gridx = 4;
        c.gridy = 11;
        frame.add(carsLabel, c);

        c.gridy = 12;
        frame.add(carsAmountField, c);

        c.gridy = 13;
        frame.add(carsSpeedField, c);
    }

    /**
     * This method sets up the information display for vans in the GUI.
     * It creates JLabels for the quantity and average speed of vans, and sets their properties.
     * The method also creates JFormattedTextFields for the van amount and speed, and sets their properties.
     * The van amount field is set to red and the speed field is set to green.
     * Both fields are set to non-editable and their values are initialized to 0.
     * Finally, it adds the JLabels and JFormattedTextFields to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the van information display components are added.
     */
    private void infoVans(JFrame frame) {

        // Create a JLabel for the van label
        JLabel vansLabel = new JLabel("Vans", JLabel.CENTER);
        vansLabel.setFont(new Font("defaut", Font.BOLD, 12));

        // Create a JFormattedTextField for the van amount
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        vansAmountField = new JFormattedTextField(numberFormat);
        vansAmountField.setValue(Integer.valueOf(0));
        vansAmountField.setBackground(Color.RED);
        vansAmountField.setEditable(false);
        vansAmountField.setPreferredSize(new Dimension(50, 20));
        vansAmountField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a JFormattedTextField for the van speed
        vansSpeedField = new JFormattedTextField(numberFormat);
        vansSpeedField.setValue(Integer.valueOf(0));
        vansSpeedField.setBackground(Color.GREEN);
        vansSpeedField.setEditable(false);
        vansSpeedField.setPreferredSize(new Dimension(50, 20));
        vansSpeedField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a GridBagConstraints object to specify the location and size of the van information display in the GUI
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 11;
        c.gridwidth = 1;
        c.insets = new Insets(0, 5, 5, 5);
        frame.add(vansLabel, c);

        c.gridy = 12;
        frame.add(vansAmountField, c);

        c.gridy = 13;
        frame.add(vansSpeedField, c);
    }

    /**
     * This method sets up the information display for lorries in the GUI.
     * It creates JLabels for the quantity and average speed of lorries, and sets their properties.
     * The method also creates JFormattedTextFields for the lorry amount and speed, and sets their properties.
     * The lorry amount field is set to red and the speed field is set to green.
     * Both fields are set to non-editable and their values are initialized to 0.
     * Finally, it adds the JLabels and JFormattedTextFields to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the lorry information display components are added.
     */
    private void infoLorries(JFrame frame) {

        // Create a JLabel for the lorry label
        JLabel lorriesLabel = new JLabel("Lorries", JLabel.CENTER);
        lorriesLabel.setFont(new Font("defaut", Font.BOLD, 12));

        // Create a JFormattedTextField for the lorry amount
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        lorriesAmountField = new JFormattedTextField(numberFormat);
        lorriesAmountField.setValue(Integer.valueOf(0));
        lorriesAmountField.setBackground(Color.RED);
        lorriesAmountField.setEditable(false);
        lorriesAmountField.setPreferredSize(new Dimension(50, 20));
        lorriesAmountField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a JFormattedTextField for the lorry speed
        lorriesSpeedField = new JFormattedTextField(numberFormat);
        lorriesSpeedField.setValue(Integer.valueOf(0));
        lorriesSpeedField.setBackground(Color.GREEN);
        lorriesSpeedField.setEditable(false);
        lorriesSpeedField.setPreferredSize(new Dimension(50, 20));
        lorriesSpeedField.setHorizontalAlignment(JFormattedTextField.CENTER);

        // Create a GridBagConstraints object to specify the location and size of the lorry information display in the GUI
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 6;
        c.gridy = 11;
        c.gridwidth = 1;
        c.insets = new Insets(0, 5, 5, 285);
        frame.add(lorriesLabel, c);

        c.gridy = 12;
        frame.add(lorriesAmountField, c);

        c.gridy = 13;
        frame.add(lorriesSpeedField, c);
    }

    /**
     * This method updates the view in the GUI with the provided image.
     * It converts the Mat image to a BufferedImage and sets it as the icon for the JLabel used for video display.
     *
     * @param image the Mat image to be displayed in the GUI.
     */
    private void updateView(Mat image) {
        imageView.setIcon(new ImageIcon(imageProcessor.toBufferedImage(image)));
    }

    /**
     * This method calculates the maximum frames per second (FPS) that can be waited for a vehicle to cross the distance between the counting and speed lines.
     * It also calculates the duration of one frame in milliseconds.
     * The method uses the distance between the lines (distanceCS), the video's FPS (videoFPS), and the speed of the vehicle to calculate these values.
     */
    public void maxWaitingFPS() {
        // Calculate the time it takes for a vehicle to cross the distance between the lines
        double time = (distanceCS / 3);
        // Calculate the maximum number of frames that can be waited for a vehicle to cross the distance
        double max = videoFPS * time;
        // Convert the maximum number of frames to an integer
        maxFPS = (int) max;

        // Calculate the duration of one frame in milliseconds
        oneFrameDuration = 1000 / (long) videoFPS;
    }

    /**
     * This method writes the vehicle data to a CSV file.
     * It initializes a CSVWriter with the provided FileWriter and a tab character as the separator.
     * It then adds the headers for the CSV file to the list of CSV data.
     * The headers include "No.", "Vehicle type", "Speed [km/h]", and "Video time [sec]".
     *
     * @param fileWriter the FileWriter used to write the CSV file.
     * @throws IOException if an I/O error occurs while writing to the CSV file.
     */
    public void writeToCSV(FileWriter fileWriter) throws IOException {
        CSVwriter = new CSVWriter(fileWriter, '\t');
        ListCSV.add("No.#Vehicle type#Speed [km/h]#Video time [sec]".split("#"));
    }

    /**
     * This method writes the initial setup for an Excel file to store vehicle data.
     * It creates a new workbook with the provided file and adds a sheet named "Counting" to the workbook.
     * The method then adds labels to the sheet for "No.", "Vehicle type", "Speed [km/h]", and "Video time [sec]".
     *
     * @param file the File object representing the Excel file to be written.
     * @throws IOException if an I/O error occurs while creating the workbook.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    public void writeToExel(File file) throws IOException, WriteException {

        workbook = Workbook.createWorkbook(file);
        sheet = workbook.createSheet("Counting", 0);
        addLabel(sheet, 0, 0, "No.");
        addLabel(sheet, 1, 0, "Vehicle type");
        addLabel(sheet, 2, 0, "Speed [km/h]");
        addLabel(sheet, 3, 0, "Video time [sec]");
    }

    /**
     * This method adds a label to a cell in an Excel sheet.
     * It creates a new Label object with the provided column, row, and text.
     * The method then adds the label to the specified sheet.
     *
     * @param sheet the WritableSheet to which the label is added.
     * @param column the column number of the cell where the label is added.
     * @param row the row number of the cell where the label is added.
     * @param text the text of the label to be added.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    private void addLabel(WritableSheet sheet, int column, int row, String text)
            throws WriteException {
        label = new jxl.write.Label(column, row, text);
        sheet.addCell(label);
    }

    /**
     * This method adds an integer value to a cell in an Excel sheet.
     * It creates a new Number object with the provided column, row, and integer.
     * The method then adds the number to the specified sheet.
     *
     * @param sheet the WritableSheet to which the number is added.
     * @param column the column number of the cell where the number is added.
     * @param row the row number of the cell where the number is added.
     * @param integer the integer value to be added.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    private void addNumberInteger(WritableSheet sheet, int column, int row, Integer integer)
            throws WriteException {

        number = new Number(column, row, integer);
        sheet.addCell(number);
    }

    /**
     * This method adds a double value to a cell in an Excel sheet.
     * It creates a new Number object with the provided column, row, and double value.
     * The method then adds the number to the specified sheet.
     *
     * @param sheet the WritableSheet to which the number is added.
     * @param column the column number of the cell where the number is added.
     * @param row the row number of the cell where the number is added.
     * @param d the double value to be added.
     * @throws WriteException if an error occurs while writing to the Excel sheet.
     */
    private void addNumberDouble(WritableSheet sheet, int column, int row, Double d)
            throws WriteException {

        number = new Number(column, row, d);
        sheet.addCell(number);
    }
    /**
     * This method calculates the speed of a vehicle based on the number of frames it takes to cross a certain distance.
     * It uses the distance between the counting and speed lines (distanceCS), the video's frames per second (videoFPS), and the speed per frame (speedPFS).
     *
     * @param speedPFS the number of frames it takes for a vehicle to cross the distance between the counting and speed lines.
     * @return the speed of the vehicle in km/h.
     */
    public double computeSpeed(int speedPFS) {
        double duration = speedPFS / videoFPS;
        double v = (distanceCS / duration) * 3.6;
        return v;
    }

    /**
     * This method calculates the real-time duration of the video in seconds.
     * It increments the frame counter (whichFrame) and calculates the time in seconds by dividing the frame counter by the video's frames per second (videoFPS).
     * The method then calls the setTimeInMinutes() method to convert the time to minutes and seconds format.
     * Finally, it returns the time in seconds.
     *
     * @return the real-time duration of the video in seconds.
     */
    private double videoRealTime() {
        whichFrame++;
        timeInSec = whichFrame / videoFPS;
        setTimeInMinutes();
        return timeInSec;
    }

    /**
     * This method converts the video time from seconds to minutes and seconds format.
     * If the time in seconds is less than 60, it sets the value of the currentTimeField to the time in seconds followed by " sec".
     * If the time in seconds is equal to or more than 60 but the seconds part is less than 60, it calculates the seconds part by subtracting the minutes part from the time in seconds.
     * It then sets the value of the currentTimeField to the minutes and seconds followed by " min " and " sec" respectively.
     * If the seconds part is equal to or more than 60, it resets the seconds part to 0 and increments the minutes part by 1.
     */
    private void setTimeInMinutes() {
        if (timeInSec < 60) {
            currentTimeField.setValue((int) timeInSec + " sec");
        } else if (second < 60) {
            second = (int) timeInSec - (60 * minutes);
            currentTimeField.setValue(minutes + " min " + second + " sec");
        } else {
            second = 0;
            minutes++;
        }
    }

    /**
     * This method creates an ImageIcon from the specified path.
     * It first gets the URL of the resource at the specified path.
     * If the URL is not null, it creates an ImageIcon from the URL and returns it.
     * If the URL is null, it prints an error message to the console and returns null.
     *
     * @param path the path to the resource.
     * @return an ImageIcon created from the resource at the specified path, or null if the resource could not be found.
     */
    private static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = GUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * This method sets up the reset functionality in the GUI.
     * It creates a JButton with the text "Replay" and sets its properties.
     * The method also adds an ActionListener to the button to handle the reset functionality.
     * When the button is clicked, it opens a confirmation dialog asking the user if they are sure they want to reset the video.
     * If the user confirms, it resets various variables and components to their initial state.
     * This includes the video capture, the video view, the play/pause button, the video processor, the reset button, the on/off buttons, the xls/csv buttons, the counting/speed line buttons, the distance field, the line points, the time variables, the vehicle fields and variables, the counter, the last TSM, the video writer, the workbook, and the CSV writer.
     * It also starts a new thread for resetting.
     * Finally, it adds the button to the JFrame using a GridBagConstraints object to specify its location and size.
     *
     * @param frame the JFrame to which the reset button is added.
     */
    private void reset(JFrame frame) {
        resetButton = new JButton("Replay");
        resetButton.setFont(new Font("defaut", Font.BOLD, 15));
        resetButton.addActionListener(event -> {

            int n = JOptionPane.showConfirmDialog(
                    frame, "Are you sure you want to reset the video?",
                    "Reset", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                loopBreaker = true;

                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(640, 360));
                updateView(currentImage);

                currentTimeField.setValue("0 sec");

                isPaused = true;
                playPauseButton.setText("Play");
                playPauseButton.setEnabled(false);
                videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);

                resetButton.setEnabled(false);

                onButton.setEnabled(true);
                offButton.setEnabled(true);

                xlsButton.setEnabled(true);
                csvButton.setEnabled(true);

                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);
                lineCount1 = null;
                lineCount2 = null;
                lineSpeed1 = null;
                lineSpeed2 = null;

                minutes = 1;
                second = 0;
                whichFrame = 0;
                timeInSec = 0;

                carsAmountField.setValue(Integer.valueOf(0));
                carsSpeedField.setValue(Integer.valueOf(0));
                vansAmountField.setValue(Integer.valueOf(0));
                vansSpeedField.setValue(Integer.valueOf(0));
                lorriesAmountField.setValue(Integer.valueOf(0));
                lorriesSpeedField.setValue(Integer.valueOf(0));

                cars = 0;
                vans = 0;
                lorries = 0;

                sumSpeedCar = 0;
                sumSpeedVan = 0;
                sumSpeedLorry = 0;

                divisorCar = 1;
                divisorVan = 1;
                divisorLorry = 1;

                counter = 0;
                lastTSM = 0;

                if (isToSave)
                    videoWriter.release();

                if (!isWritten) {
                    try {
                        workbook.write();
                        workbook.close();
                    } catch (IOException | WriteException e) {
                        e.printStackTrace();
                    }
                    if (!isExcelToWrite) {
                        try {
                            CSVwriter.writeAll(ListCSV);
                            CSVwriter.close();
                            new File(savePath + "\\Results.xls").delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    isWritten = true;
                }

                Thread reseting = new Thread(new Reseting());
                reseting.start();
                loopBreaker = false;
            }

        });

        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(5, 60, 5, 60);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;

        frame.add(resetButton, c);
    }

    /**
     * This class implements the Runnable interface and overrides the run method.
     * It is used to reset the state of the application when the reset button is clicked.
     * The run method is executed in a separate thread to avoid blocking the main thread.
     */
    private class Reseting implements Runnable {

        /**
         * The run method is called when the thread is started.
         * It enters an infinite loop that checks if the counting and speed lines have been set.
         * If both lines have been set, it enables the play/pause and reset buttons, and disables the on/off and xls/csv buttons.
         * It also checks if the save video option is on, and if so, it creates a new VideoWriter for the saved video.
         * The method then starts a new thread for the main loop of the application.
         * It sets the save path for the results file and tries to write the initial setup for the Excel file.
         * If the Excel option is not selected, it tries to write the initial setup for the CSV file.
         * Finally, it sets the isWritten flag to false and breaks the infinite loop.
         */

        @Override
        public void run() {

            while (true) {
                if (lineSpeed2 != null && lineCount2 != null) {
                    playPauseButton.setEnabled(true);
                    resetButton.setEnabled(true);

                    onButton.setEnabled(false);
                    offButton.setEnabled(false);

                    xlsButton.setEnabled(false);
                    csvButton.setEnabled(false);

                    if (saveFlag.equals(onSaveVideo)) {
                        videoWriter = new VideoWriter(savePath + "\\Video.avi", VideoWriter.fourcc('P', 'I', 'M', '1'), videoFPS, new Size(640, 360));
                    }

                    Thread mainLoop = new Thread(new Loop());
                    mainLoop.start();

                    String xlsSavePath = savePath + "\\Results.xls";
                    fileToSaveXLS = new File(xlsSavePath);
                    try {
                        writeToExel(fileToSaveXLS);
                    } catch (IOException | WriteException e) {
                        e.printStackTrace();
                    }

                    if (!isExcelToWrite) {
                        String csvSavePath = savePath + "\\Results.csv";
                        try {
                            filetoSaveCSV = new FileWriter(csvSavePath);
                            writeToCSV(filetoSaveCSV);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    isWritten = false;

                    break;
                }
            }
        }
    }


    /**
     * This method sets up the video saving functionality in the GUI.
     * It creates two JRadioButtons for the "On" and "Off" options and sets their properties.
     * The "On" button is set to not selected and the "Off" button is set to selected by default.
     * The method also creates a ButtonGroup and adds the two JRadioButtons to it.
     * An ActionListener is added to the JRadioButtons to handle the selection change.
     * When a button is selected, it sets the saveFlag to the action command of the selected button and sets the isToSave flag accordingly.
     * The method then creates a JPanel with a GridLayout and adds the JRadioButtons to it.
     * It also creates a JLabel for the "Saving video :" label and adds it to the JFrame.
     * Finally, it adds the JPanel to the JFrame using a GridBagConstraints object to specify its location and size.
     *
     * @param frame the JFrame to which the video saving components are added.
     */
    private void setupSaveVideo(JFrame frame) {

        onButton = new JRadioButton(onSaveVideo);
        onButton.setMnemonic(KeyEvent.VK_O);
        onButton.setActionCommand(onSaveVideo);
        onButton.setSelected(false);
        onButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        offButton = new JRadioButton(offSaveVideo);
        offButton.setMnemonic(KeyEvent.VK_F);
        offButton.setActionCommand(offSaveVideo);
        offButton.setSelected(true);
        offButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(onButton);
        group.add(offButton);

        ActionListener operationChangeListener = event -> {
            saveFlag = event.getActionCommand();
            isToSave = (saveFlag.equals(onSaveVideo));
        };

        onButton.addActionListener(operationChangeListener);
        offButton.addActionListener(operationChangeListener);

        GridLayout gridRowLayout = new GridLayout(1, 0);
        JPanel saveOperationPanel = new JPanel(gridRowLayout);

        JLabel fillLabel = new JLabel("Saving video : ", JLabel.CENTER);
        fillLabel.setFont(new Font("defaut", Font.BOLD, 15));
        saveOperationPanel.add(onButton);
        saveOperationPanel.add(offButton);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);

        c.gridx = 0;
        c.gridy = 1;
        frame.add(fillLabel, c);

        c.gridx = 1;
        frame.add(saveOperationPanel, c);
    }

    /**
     * This method sets up the file type selection functionality in the GUI for saving results.
     * It creates two JRadioButtons for the "XLS" and "CSV" options and sets their properties.
     * The "XLS" button is set to selected and the "CSV" button is set to not selected by default.
     * The method also creates a ButtonGroup and adds the two JRadioButtons to it.
     * An ActionListener is added to the JRadioButtons to handle the selection change.
     * When a button is selected, it sets the writeFlag to the action command of the selected button and sets the isExcelToWrite flag accordingly.
     * The method then creates a JPanel with a GridLayout and adds the JRadioButtons to it.
     * It also creates a JLabel for the "File results :" label and adds it to the JFrame.
     * Finally, it adds the JPanel to the JFrame using a GridBagConstraints object to specify its location and size.
     *
     * @param frame the JFrame to which the file type selection components are added.
     */
    private void setupWriteType(JFrame frame) {

        xlsButton = new JRadioButton(xlsWriteResults);
        xlsButton.setMnemonic(KeyEvent.VK_O);
        xlsButton.setActionCommand(xlsWriteResults);
        xlsButton.setSelected(true);
        xlsButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        csvButton = new JRadioButton(csvWriteResults);
        csvButton.setMnemonic(KeyEvent.VK_F);
        csvButton.setActionCommand(csvWriteResults);
        csvButton.setSelected(false);
        csvButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(xlsButton);
        group.add(csvButton);

        ActionListener operationChangeListener = event -> {
            writeFlag = event.getActionCommand();
            isExcelToWrite = (writeFlag.equals(xlsWriteResults)) ? true : false;
        };

        xlsButton.addActionListener(operationChangeListener);
        csvButton.addActionListener(operationChangeListener);

        GridLayout gridRowLayout = new GridLayout(1, 0);
        JPanel writeOperationPanel = new JPanel(gridRowLayout);

        JLabel writeLabel = new JLabel("File results :", JLabel.CENTER);
        writeLabel.setFont(new Font("defaut", Font.BOLD, 15));
        writeOperationPanel.add(xlsButton);
        writeOperationPanel.add(csvButton);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 0);

        c.gridx = 0;
        c.gridy = 2;
        frame.add(writeLabel, c);

        c.gridx = 1;
        frame.add(writeOperationPanel, c);
    }

    /**
     * This method sets up the distance selection functionality in the GUI.
     * It creates a JLabel for the "Distance [m] :" label and sets its properties.
     * The method also creates a JSpinner for the distance selection and sets its properties.
     * The JSpinner is initialized with a SpinnerNumberModel that has the initial value set to distanceCS, the minimum value set to 0, the maximum value set to 10, and the step size set to 0.5.
     * The JSpinner is set to align to the left and its preferred size is set to 55x26.
     * A ChangeListener is added to the JSpinner to handle the distance selection change.
     * When the value of the JSpinner changes, it sets the distanceCS to the new value.
     * Finally, it adds the JLabel and JSpinner to the JFrame using a GridBagConstraints object to specify their location and size.
     *
     * @param frame the JFrame to which the distance selection components are added.
     */
    private void setupDistanceBetweenLines(JFrame frame) {
        JLabel distanceBLLabel = new JLabel("Distance [m] :", JLabel.RIGHT);
        distanceBLLabel.setFont(new Font("defaut", Font.BOLD, 15));

        distanceBLfield = new JSpinner(new SpinnerNumberModel(distanceCS, 0, 10, 0.5));
        distanceBLfield.setAlignmentX(Component.LEFT_ALIGNMENT);
        distanceBLfield.setPreferredSize(new Dimension(55, 26));

        distanceBLfield.addChangeListener(e ->
                distanceCS = (double) distanceBLfield.getValue());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.insets = new Insets(0, 5, 0, 0);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        frame.add(distanceBLLabel, c);

        c.insets = new Insets(0, 0, 0, 30);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        frame.add(distanceBLfield, c);
    }

    /**
     * This method sets up the counting line selection functionality in the GUI.
     * It creates a JButton with the text "counting line" and sets its properties.
     * The button's preferred size is set to 120x40, its font is set to bold with size 15, and its foreground and background colors are set to red.
     * The method also adds an ActionListener to the button to handle the counting line selection.
     * When the button is clicked, it disables the counting line button and the speed line button, sets the mouseListenertIsActive flag to true, the startDraw flag to false, and adds a MouseListener and a MouseMotionListener to the imageView.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the button in the GUI.
     * Finally, it adds the button to the JFrame.
     *
     * @param frame the JFrame to which the counting line selection button is added.
     */
    private void selectCountingLine(JFrame frame) {

        countingLineButton = new JButton("counting line  ");

        countingLineButton.setPreferredSize(new Dimension(120, 40));

        countingLineButton.setPreferredSize(new Dimension(10, 40));

        countingLineButton.setFont(new Font("defaut", Font.BOLD, 15));
        countingLineButton.setForeground(Color.RED);
        countingLineButton.setBackground(Color.RED);
        countingLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
        countingLineButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 5, 10, 5);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;

        frame.add(countingLineButton, c);
    }

    /**
     * This method sets up the speed line selection functionality in the GUI.
     * It creates a JButton with the text "Speed Line" and sets its properties.
     * The button's preferred size is set to 120x40, its font is set to bold with size 15, and its foreground and background colors are set to green.
     * The method also adds an ActionListener to the button to handle the speed line selection.
     * When the button is clicked, it disables the counting line button and the speed line button, sets the mouseListenertIsActive2 flag to true, the startDraw flag to false, and adds a MouseListener and a MouseMotionListener to the imageView.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the button in the GUI.
     * Finally, it adds the button to the JFrame.
     *
     * @param frame the JFrame to which the speed line selection button is added.
     */
    private void selectSpeedLine(JFrame frame) {

        speedLineButton = new JButton("Speed Line");

        speedLineButton.setPreferredSize(new Dimension(120, 40));
        speedLineButton.setFont(new Font("defaut", Font.BOLD, 15));
        speedLineButton.setForeground(Color.GREEN);
        speedLineButton.setBackground(Color.GREEN);
        speedLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive2 = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
        speedLineButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 5);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;

        frame.add(speedLineButton, c);
    }

    /**
     * This method handles the drawing of counting lines on the video.
     * It takes an event type and a point as parameters.
     * If the event type is 1, it checks if the startDraw flag is false.
     * If it is, it sets the lineCount1 point to the provided point and sets the startDraw flag to true.
     * If the startDraw flag is true, it sets the lineCount2 point to the provided point, sets the startDraw flag to false, disables the mouse listener, and enables the counting line and speed line buttons.
     * If the event type is 0 and the startDraw flag is true, it clones the current image, draws a line from lineCount1 to the provided point, and updates the view with the copied image.
     * If the lineSpeed1 and lineSpeed2 points are not null, it also draws a line from lineSpeed1 to lineSpeed2.
     *
     * @param event the type of event. 1 for a click event, 0 for a mouse move event.
     * @param point the point where the event occurred.
     */
    public void call(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineCount1 = point;
                startDraw = true;
            } else {
                lineCount2 = point;
                startDraw = false;
                mouseListenertIsActive = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineCount1, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed1 != null && lineSpeed2 != null)
                Imgproc.line(copiedImage, lineSpeed1, lineSpeed2, new Scalar(0, 255, 0), 1);
            updateView(copiedImage);
        }
    }

    /**
     * This method handles the drawing of speed lines on the video.
     * It takes an event type and a point as parameters.
     * If the event type is 1, it checks if the startDraw flag is false.
     * If it is, it sets the lineSpeed1 point to the provided point and sets the startDraw flag to true.
     * If the startDraw flag is true, it sets the lineSpeed2 point to the provided point, sets the startDraw flag to false, disables the mouse listener, and enables the counting line and speed line buttons.
     * If the event type is 0 and the startDraw flag is true, it clones the current image, draws a line from lineSpeed1 to the provided point, and updates the view with the copied image.
     * If the lineCount1 and lineCount2 points are not null, it also draws a line from lineCount1 to lineCount2.
     *
     * @param event the type of event. 1 for a click event, 0 for a mouse move event.
     * @param point the point where the event occurred.
     */
    private void call2(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineSpeed1 = point;
                startDraw = true;
            } else {
                lineSpeed2 = point;
                startDraw = false;
                mouseListenertIsActive2 = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineSpeed1, point, new Scalar(0, 255, 0), 1);
            if (lineCount1 != null && lineCount2 != null)
                Imgproc.line(copiedImage, lineCount1, lineCount2, new Scalar(0, 0, 255), 1);
            updateView(copiedImage);
        }
    }

    /**
     * This is a MouseListener implementation for handling mouse events.
     * It overrides the five methods of the MouseListener interface: mouseClicked, mousePressed, mouseReleased, mouseEntered, and mouseExited.
     * The mousePressed method is the only one with functionality, the others are empty.
     * In the mousePressed method, it checks if the mouseListenertIsActive or mouseListenertIsActive2 flags are true.
     * If mouseListenertIsActive is true, it calls the call method with the button type and the point of the mouse event.
     * If mouseListenertIsActive2 is true, it calls the call2 method with the button type and the point of the mouse event.
     */
    private MouseListener ml = new MouseListener() {
        /**
         * This method is called when the mouse is clicked.
         * It is currently empty and does not perform any action.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseClicked(MouseEvent e) {
        }

        /**
         * This method is called when a mouse button is pressed.
         * If the mouseListenertIsActive flag is true, it calls the call method with the button type and the point of the mouse event.
         * If the mouseListenertIsActive2 flag is true, it calls the call2 method with the button type and the point of the mouse event.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mousePressed(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }

        /**
         * This method is called when a mouse button is released.
         * It is currently empty and does not perform any action.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseReleased(MouseEvent e) {
        }

        /**
         * This method is called when the mouse enters a component.
         * It is currently empty and does not perform any action.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseEntered(MouseEvent e) {
        }

        /**
         * This method is called when the mouse exits a component.
         * It is currently empty and does not perform any action.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseExited(MouseEvent e) {
        }
    };

    /**
     * This is a MouseMotionListener implementation for handling mouse motion events.
     * It overrides the two methods of the MouseMotionListener interface: mouseDragged and mouseMoved.
     * The mouseDragged method is currently empty and does not perform any action.
     * In the mouseMoved method, it checks if the mouseListenertIsActive or mouseListenertIsActive2 flags are true.
     * If mouseListenertIsActive is true, it calls the call method with the button type and the point of the mouse event.
     * If mouseListenertIsActive2 is true, it calls the call2 method with the button type and the point of the mouse event.
     */
    private MouseMotionListener ml2 = new MouseMotionListener() {
        /**
         * This method is called when the mouse is dragged.
         * It is currently empty and does not perform any action.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseDragged(MouseEvent e) {

        }

        /**
         * This method is called when the mouse is moved.
         * If the mouseListenertIsActive flag is true, it calls the call method with the button type and the point of the mouse event.
         * If the mouseListenertIsActive2 flag is true, it calls the call2 method with the button type and the point of the mouse event.
         *
         * @param e the MouseEvent that occurred.
         */
        public void mouseMoved(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }
    };

    /**
     * This method sets up the image threshold selection functionality in the GUI.
     * It creates a JLabel for the "Video threshold:" label and sets its alignment to right.
     * The method also creates a JSpinner for the image threshold selection and sets its properties.
     * The JSpinner is initialized with a SpinnerNumberModel that has the initial value set to imageThreshold, the minimum value set to 0, the maximum value set to 10000, and the step size set to 5.
     * The JSpinner is set to align to the left.
     * A ChangeListener is added to the JSpinner to handle the image threshold selection change.
     * When the value of the JSpinner changes, it sets the imageThreshold to the new value and updates the videoProcessor's image threshold.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the JLabel and JSpinner in the GUI.
     * Finally, it adds the JLabel and JSpinner to the JFrame.
     *
     * @param frame the JFrame to which the image threshold selection components are added.
     */
    private void setupImageThreshold(JFrame frame) {
        JLabel imgThresholdLabel = new JLabel("Video threshold:", JLabel.RIGHT);

        imgThresholdField = new JSpinner(new SpinnerNumberModel(imageThreshold, 0, 10000, 5));
        imgThresholdField.setAlignmentX(Component.LEFT_ALIGNMENT);

        imgThresholdField.addChangeListener(e -> {
            imageThreshold = (double) imgThresholdField.getValue();
            videoProcessor.setImageThreshold(imageThreshold);
        });

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        frame.add(imgThresholdLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        frame.add(imgThresholdField, c);
    }

    /**
     * This method sets up the video history selection functionality in the GUI.
     * It creates a JLabel for the "History:" label and sets its alignment to right.
     * The method also creates a JSpinner for the video history selection and sets its properties.
     * The JSpinner is initialized with a SpinnerNumberModel that has the initial value set to history, the minimum value set to 0, the maximum value set to 100000, and the step size set to 50.
     * The JSpinner is set to align to the left.
     * A ChangeListener is added to the JSpinner to handle the video history selection change.
     * When the value of the JSpinner changes, it sets the history to the new value and updates the videoProcessor's history.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the JLabel and JSpinner in the GUI.
     * Finally, it adds the JLabel and JSpinner to the JFrame.
     *
     * @param frame the JFrame to which the video history selection components are added.
     */
    private void setupVideoHistory(JFrame frame) {
        JLabel videoHistoryLabel = new JLabel("History:", JLabel.RIGHT);

        videoHistoryField = new JSpinner(new SpinnerNumberModel(history, 0, 100000, 50));
        videoHistoryField.setAlignmentX(Component.LEFT_ALIGNMENT);

        videoHistoryField.addChangeListener(e -> {
            history = (int) videoHistoryField.getValue();
            videoProcessor.setHistory(history);
        });

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 1;
        frame.add(videoHistoryLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        frame.add(videoHistoryField, c);
    }

    /**
     * This method sets up the area threshold selection functionality in the GUI.
     * It creates a JLabel for the "Area threshold:" label and sets its alignment to right.
     * The method also creates a JSpinner for the area threshold selection and sets its properties.
     * The JSpinner is initialized with a SpinnerNumberModel that has the initial value set to areaThreshold, the minimum value set to 0, the maximum value set to 100000, and the step size set to 50.
     * The JSpinner is set to align to the left.
     * A ChangeListener is added to the JSpinner to handle the area threshold selection change.
     * When the value of the JSpinner changes, it sets the areaThreshold to the new value.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the JLabel and JSpinner in the GUI.
     * Finally, it adds the JLabel and JSpinner to the JFrame.
     *
     * @param frame the JFrame to which the area threshold selection components are added.
     */
    private void setupAreaThreshold(JFrame frame) {
        JLabel areaThresholdLabel = new JLabel("Area threshold:", JLabel.RIGHT);

        final JSpinner areaThresholdField = new JSpinner(new SpinnerNumberModel(areaThreshold, 0, 100000, 50));
        areaThresholdField.setAlignmentX(Component.LEFT_ALIGNMENT);

        areaThresholdField.addChangeListener(e ->
                areaThreshold = (int) areaThresholdField.getValue());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 1;
        frame.add(areaThresholdLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        frame.add(areaThresholdField, c);
    }

    /**
     * This method sets up the vehicle size threshold selection functionality in the GUI.
     * It creates a JLabel for the "Vehicle size threshold:" label and sets its alignment to right.
     * The method also creates a JSpinner for the vehicle size threshold selection and sets its properties.
     * The JSpinner is initialized with a SpinnerNumberModel that has the initial value set to vehicleSizeThreshold, the minimum value set to 0, the maximum value set to 100000, and the step size set to 100.
     * The JSpinner is set to align to the left.
     * A ChangeListener is added to the JSpinner to handle the vehicle size threshold selection change.
     * When the value of the JSpinner changes, it sets the vehicleSizeThreshold to the new value.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the JLabel and JSpinner in the GUI.
     * Finally, it adds the JLabel and JSpinner to the JFrame.
     *
     * @param frame the JFrame to which the vehicle size threshold selection components are added.
     */
    private void setupVehicleSizeThreshold(JFrame frame) {
        JLabel vehicleSizeThresholdLabel = new JLabel("Vehicle size threshold:", JLabel.RIGHT);

        final JSpinner vehicleSizeThresholdField = new JSpinner(new SpinnerNumberModel(vehicleSizeThreshold, 0, 100000, 100));
        vehicleSizeThresholdField.setAlignmentX(Component.LEFT_ALIGNMENT);

        vehicleSizeThresholdField.addChangeListener(e ->
                vehicleSizeThreshold = (int) vehicleSizeThresholdField.getValue());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth = 1;
        frame.add(vehicleSizeThresholdLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        frame.add(vehicleSizeThresholdField, c);
    }

    /**
     * This method initializes the Background Subtraction (BGS) view in the GUI.
     * It creates a new JFrame for the BGS view and a JLabel to display the BGS image.
     * The JLabel is added to the JFrame.
     * A local image is created with a size of 430x240 and a color of white.
     * The JLabel's icon is set to this local image.
     * The JFrame is then made visible and packed to fit its components.
     * A WindowListener is added to the JFrame to handle the window closing event.
     * When the window is closed, the BGSButton is enabled and the isBGSview flag is set to false.
     */
    private void initBGSview() {
        frameBGS = new JFrame("BGS View");
        BGSview = new JLabel();
        frameBGS.add(BGSview);
        Mat localImage = new Mat(new Size(430, 240), CvType.CV_8UC3, new Scalar(255, 255, 255));
        BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(localImage)));
        frameBGS.setVisible(true);
        frameBGS.pack();

        frameBGS.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                BGSButton.setEnabled(true);
                isBGSview = false;
            }
        });

    }

    /**
     * This method sets up the Background Subtraction (BGS) visibility functionality in the GUI.
     * It creates a JButton with the text "Substraction" and sets its properties.
     * The button's preferred size is set to 300x35, its font is set to bold with size 15, and its background color is set to black.
     * The method also adds an ActionListener to the button to handle the BGS visibility.
     * When the button is clicked, it calls the initBGSview method, disables the BGSButton, sets the isBGSview flag to true, and updates the button's properties.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the button in the GUI.
     * Finally, it adds the button to the JFrame.
     *
     * @param frame the JFrame to which the BGS visibility button is added.
     */

    private void setupBGSvisibility(JFrame frame) {
        BGSButton = new JButton("Substraction");


        BGSButton.setPreferredSize(new Dimension(300, 35));

        BGSButton.setFont(new Font("defaut", Font.BOLD, 15));
        BGSButton.setBackground(Color.BLACK);
        BGSButton.addActionListener(event -> {
            initBGSview();
            BGSButton.setEnabled(false);
            BGSButton.setPreferredSize(new Dimension(300, 35));

            BGSButton.setFont(new Font("defaut", Font.BOLD, 15));
            isBGSview = true;
            BGSButton.setPreferredSize(new Dimension(300, 35));

            BGSButton.setFont(new Font("defaut", Font.BOLD, 15));
        });

        GridBagConstraints c = new GridBagConstraints();
        BGSButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 2;

        frame.add(BGSButton, c);

    }

    /**
     * This method sets up the current time display functionality in the GUI.
     * It creates a JLabel for the "Real time:" label and sets its alignment to right.
     * The method also creates a JFormattedTextField for the current time display and sets its properties.
     * The JFormattedTextField is initialized with a value of "0 sec" and is set to center alignment and read-only.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the JLabel and JFormattedTextField in the GUI.
     * Finally, it adds the JLabel and JFormattedTextField to the JFrame.
     *
     * @param frame the JFrame to which the current time display components are added.
     */
    private void currentTime(JFrame frame) {

        JLabel currentTimeLabel = new JLabel("Real time:", JLabel.RIGHT);
        currentTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        currentTimeField = new JFormattedTextField();
        currentTimeField.setValue("0 sec");
        currentTimeField.setHorizontalAlignment(JFormattedTextField.CENTER);
        currentTimeField.setEditable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;


        c.gridx = 0;
        c.gridy = 13;

        c.gridx = 6;
        c.gridy = 12;

        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 20);
        frame.add(currentTimeLabel, c);
        c.insets = new Insets(0, 0, 0, 40);

        c.gridx = 1;

        c.gridx = 7;

        frame.add(currentTimeField, c);
    }

    /**
     * This method sets up the real time processing functionality in the GUI.
     * It creates a JButton with the text "Real time OFF" and sets its properties.
     * The button's preferred size is set to 300x35, its font is set to bold with size 15, and its background color is set to black.
     * The method also adds an ActionListener to the button to handle the real time processing.
     * When the button is clicked, it toggles the isProcessInRealTime flag and updates the button's text.
     * The method then creates a GridBagConstraints object and sets its properties to specify the location and size of the button in the GUI.
     * Finally, it adds the button to the JFrame.
     *
     * @param frame the JFrame to which the real time processing button is added.
     */
    private void setupRealTime(JFrame frame) {

        realTimeButton = new JButton("Real time OFF");


        realTimeButton.setPreferredSize(new Dimension(300, 35));
        realTimeButton.setFont(new Font("defaut", Font.BOLD, 15));
        realTimeButton.setBackground(Color.BLACK);


        realTimeButton.addActionListener(event -> {
            if (isProcessInRealTime) {
                realTimeButton.setPreferredSize(new Dimension(300, 35));
                realTimeButton.setFont(new Font("defaut", Font.BOLD, 15));
                isProcessInRealTime = false;
                realTimeButton.setPreferredSize(new Dimension(300, 35));
                realTimeButton.setFont(new Font("defaut", Font.BOLD, 15));
                realTimeButton.setText("Ral time OFF");
            } else {
                realTimeButton.setPreferredSize(new Dimension(300, 35));
                realTimeButton.setFont(new Font("defaut", Font.BOLD, 15));
                isProcessInRealTime = true;
                realTimeButton.setPreferredSize(new Dimension(300, 35));
                realTimeButton.setFont(new Font("defaut", Font.BOLD, 15));
                realTimeButton.setText("Real time ON");
            }

        });
        realTimeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 11;
        c.gridwidth = 2;
        c.gridheight = 2;

        frame.add(realTimeButton, c);
    }

    /**
     * This method sets the look and feel of the UI to match the system's look and feel.
     * It calls the UIManager's setLookAndFeel method with the system's look and feel class name.
     * If the system's look and feel class name cannot be found, or if an error occurs while attempting to create an instance of the class, or if there's insufficient access to invoke the look and feel class, or if the look and feel class fails to initialize, it prints the stack trace of the exception.
     */

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}