
package controller;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import com.sun.istack.internal.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.FaceDetectorUtils;
import utils.StringUtils;


public class FaceDetectionController {

	@FXML
	private Button cameraButton;
	@FXML
	private ImageView originalFrame;
	@FXML
	private RadioButton haarClassifier;
	@FXML
	private RadioButton lbpClassifier;
	@FXML
	private CheckBox gaussianBlur;
	@FXML
	private CheckBox thresh;
	@FXML
	private CheckBox erosion;
	@FXML
	private CheckBox dilation;
	@FXML
	private CheckBox hsvBGR;
	@FXML
	private CheckBox hsvBGRFull;
	@FXML
	private CheckBox hsvRGB;
	@FXML
	private CheckBox hsvRGBFull;
	@FXML
	private CheckBox clockwise;
	@FXML
	private ToggleButton toggleGrayScale;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture;
	// a flag to change the button behavior
	private boolean isCameraActive;

	private CascadeClassifier faceCascade;
	private CascadeClassifier eyesCascade;
	private int absoluteFaceSize;
	private Map<CheckBox, Boolean> hsvMap = new HashMap<>();

	private static final String LBP_CASCADE_RESOURCE = StringUtils.CascadeResources.LBP.getValue();
	private static final String HAAR_CASCADE_RESOURCE = StringUtils.CascadeResources.HAAR.getValue();
	private static final String EYES_CASCADE_RESOURCE = StringUtils.CascadeResources.EYES.getValue();
	Mat frame;
	private Broker broker;
	Logger logger = Logger.getLogger(FaceDetectionController.class);
	Thread thread;


	// CascadeClassifier eyes_cascade;

	/**
	 * Init the controller, at start time
	 * 
	 * @throws FileNotFoundException
	 */
	public void init(Broker broker) throws FileNotFoundException {
		this.broker = broker;
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.eyesCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;


		hsvMap.put(hsvBGR, hsvBGR.isSelected());
		hsvMap.put(hsvBGRFull, hsvBGRFull.isSelected());
		hsvMap.put(hsvRGB, hsvRGB.isSelected());
		hsvMap.put(hsvRGBFull, hsvRGBFull.isSelected());
		hsvMap.put(clockwise, clockwise.isSelected());
		log();
	}


	private void log() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(loadEyesCascadeResources());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(loadHaarCascadeResources());
		stringBuilder.append(System.lineSeparator());
		stringBuilder.append(loadLBPCascadeResources());
		stringBuilder.append(System.lineSeparator());
		System.out.println(stringBuilder.toString());
	}


	private String loadEyesCascadeResources() {
		return eyesCascade.load(EYES_CASCADE_RESOURCE) ? "Successfully loading eyes cascade" : "Error loading eyes cascade";
	}


	private String loadLBPCascadeResources() {
		return faceCascade.load(LBP_CASCADE_RESOURCE) ? "Successfully loading LBP cascade" : "Error loading LBP cascade";
	}


	private String loadHaarCascadeResources() {
		return faceCascade.load(HAAR_CASCADE_RESOURCE) ? "Successfully loading Haar cascade" : "Error loading Haar cascade";
	}



	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		// set a fixed width for the frame
		originalFrame.setFitWidth(900);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);

		if( !this.isCameraActive)
		{
			// disable setting checkboxes
			this.haarClassifier.setDisable(true);
			this.lbpClassifier.setDisable(true);

			// start the video capture
			capture.open(0);

			// is the video stream available?
			if(capture.isOpened())
			{
				isCameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = ()-> {
					// Mat frame = grabFrame();
					frame = grabFrame();
					// convert the Mat object (OpenCV) to Image (JavaFX)
					Image imageToShow = FaceDetectorUtils.convertMatToImage(frame);
					originalFrame.setImage(imageToShow);
				};
				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				// update the button content
				cameraButton.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Failed to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			isCameraActive = false;
			// update again the button content
			cameraButton.setText("Start Camera");
			// enable classifiers checkboxes
			haarClassifier.setDisable(false);
			lbpClassifier.setDisable(false);

			stopAcquisition();
			// clean the frame
			this.originalFrame.setImage(null);
		}
	}


	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Mat grabFrame() {
		Mat frame = new Mat();

		// check if the capture is open
		if(this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if( !frame.empty())
				{
					// face detection
					this.detectAndDisplay(frame);
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return frame;
	}


	/**
	 * Method for face detection and tracking
	 * 
	 * @param frame
	 *            it looks for faces in this frame
	 * @throws InterruptedExceptio
	 * @throws FileNotFoundException
	 *             n
	 */
	// TODO:REFACTORING!!!!!!!!!!!!!!!!
	private void detectAndDisplay(Mat frame) throws InterruptedException, FileNotFoundException {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * 5 + 1, 2 * 5 + 1));

		Mat frame_gray = new Mat();
		Imgproc.cvtColor(frame, frame_gray, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.equalizeHist(frame_gray, frame_gray);

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (20% of the frame height, in our case)
		if(this.absoluteFaceSize == 0)
		{
			int height = grayFrame.rows();
			if(Math.round(height * 0.2f) > 0)
			{
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}


		if(toggleGrayScale.isSelected())
		{
			hsvMap.forEach((k, v)-> {
				k.setSelected(false);
				k.setDisable(true);
			});
			// set grayscale with white frame
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
		}
		else
		{
			hsvMap.forEach((k, v)-> {
				k.setDisable(false);
			});
		}
		if(gaussianBlur.isSelected())
		{ // gaussian blur
			Imgproc.GaussianBlur(frame, frame, new Size(45, 45), 0);
		}
		if(thresh.isSelected())
		{ // thresh
			Imgproc.threshold(frame, frame, 127, 255, Imgproc.THRESH_BINARY_INV);
		}
		if(erosion.isSelected())
		{ // erosion
			Imgproc.erode(frame, frame, element);
		}
		if(dilation.isSelected())
		{ // dilation
			Imgproc.dilate(frame, frame, element);
		}
		if(hsvBGR.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2BGR);
		}
		if(hsvBGRFull.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2BGR_FULL);
		}
		if(hsvRGB.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2RGB);
		}
		if(hsvRGBFull.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2RGB_FULL);
		}
		if(clockwise.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.CV_COUNTER_CLOCKWISE);
		}

		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		// each rectangle in faces is a face: draw them!
		Runnable faceCollector = ()-> {
			for(Rect face : faces.toArray())
			{
				Point faceCenter = new Point(face.x + face.width * 0.5, face.y + face.height * 0.5);
				Imgproc.ellipse(frame, faceCenter, new Size(face.width * 0.5, face.height * 0.5), 0, 0, 360, new Scalar(255, 255, 25), 1, 8, 0);
				// Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(255, 255, 0), 1);

				// Mat faceROI = frame_gray.submat(face);
				// MatOfRect eyes = new MatOfRect();
				// // -- In each face, detect eyes
				// eyesCascade.detectMultiScale(faceROI, eyes, 1.1, 2, 0, new Size(20, 20), new Size());
				// for(Rect eye : eyes.toArray())
				// {
				// Point eyeCenter = new Point(face.x + eye.x + eye.width * 0.5, face.y + eye.y + eye.height * 0.5);
				// int radius = (int)Math.round((eye.width + eye.height) * 0.25);
				// Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 1, 8, 0);
				// }

				// broker.put(frame);

				try
				{
					// // broker.put(frame);
					//
					String facename = FaceDetectorUtils.saveMatТоImage(frame);
					broker.putFace(facename);

					System.out.println("Producer is producing.." + Thread.currentThread()
							.getName());
					// Thread.sleep(2000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};

		new Thread(faceCollector).start();
	}


	@FXML
	public void grayScaleSelected() {

	}


	@FXML
	public void gaussianBlurSelected(Event event) {

	}


	@FXML
	public void gaussianBlurOnMouseReleased(Event event) {

	}


	@FXML
	public void treshSelected() {

	}


	@FXML
	public void erosionSelected() {

	}


	@FXML
	public void dilationSelected() {}


	@FXML
	public void hsvBGRSelected() {

	}


	@FXML
	public void hsvBGRFullSelected() {

	}


	@FXML
	public void hsvRGBSelected() {

	}


	@FXML
	public void hsvRGBFullSelected(Event event) {
		if(hsvRGBFull.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_HSV2RGB_FULL);
		}

	}


	@FXML
	protected void clockwiseSelected(ActionEvent event) {
		if(clockwise.isSelected())
		{
			Imgproc.cvtColor(frame, frame, Imgproc.CV_COUNTER_CLOCKWISE);
		}
	}



	/**
	 * The action triggered by selecting the Haar Classifier checkbox. It loads the trained set to be used for frontal face detection.
	 */
	@FXML
	protected void haarSelected(Event event) {
		// check whether the lpb checkbox is selected and deselect it
		if(lbpClassifier.isSelected())
		{
			lbpClassifier.setSelected( !lbpClassifier.isSelected());
		}

		checkboxSelection(HAAR_CASCADE_RESOURCE);
	}


	/**
	 * The action triggered by selecting the LBP Classifier checkbox. It loads the trained set to be used for frontal face detection.
	 */
	@FXML
	protected void lbpSelected(Event event) {
		// check whether the haar checkbox is selected and deselect it
		if(haarClassifier.isSelected())
		{
			haarClassifier.setSelected( !haarClassifier.isSelected());
		}

		checkboxSelection(LBP_CASCADE_RESOURCE);
	}


	/**
	 * Method for loading a classifier trained set from disk
	 * 
	 * @param classifierPath
	 *            the path on disk where a classifier trained set is located
	 */
	private void checkboxSelection(String classifierPath) {
		// load the classifier(s)
		faceCascade.load(classifierPath);
		// now the video capture can start
		cameraButton.setDisable(false);
	}


	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	public void stopAcquisition() {
		if(this.timer != null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch(InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if(this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
}
