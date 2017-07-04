
package controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import utils.StringUtils;

public class FaceDetectionControllerTest {

	private static CascadeClassifier cascadeClassifier;
	private static Mat image;

	private static final String LBP_CASCADE_RESOURCE = StringUtils.CascadeResources.LBP.getValue();
	private static final String HAAR_CASCADE_RESOURCE = StringUtils.CascadeResources.HAAR.getValue();
	private static final String EYES_CASCADE_RESOURCE = StringUtils.CascadeResources.EYES.getValue();


	@BeforeClass
	public static void setUp() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		cascadeClassifier = spy(new CascadeClassifier());
		image = mock(Mat.class);
	}


	@After
	public void reset() {
		// Mockito.reset(cascadeClassifier);
	}


	@AfterClass
	public static void releaseObjects() {
		cascadeClassifier = null;
		image = null;
	}


	@Test
	public void testLoadCascadeResources() {
		System.out.println("Loading LBP cascade resource...");
		assertTrue(cascadeClassifier.load(LBP_CASCADE_RESOURCE));
		System.out.println("Done!");

		System.out.println("Loading Haar cascade resource...");
		assertTrue(cascadeClassifier.load(HAAR_CASCADE_RESOURCE));
		System.out.println("Done!");

		System.out.println("Loading eyes cascade resource...");
		assertTrue(cascadeClassifier.load(EYES_CASCADE_RESOURCE));
		System.out.println("Done!");
	}


	@Test
	public void testDetectFaceFromImage() {
		System.out.println("\nRunning FaceDetector");
		final String testPath = StringUtils.RESOURCES_PATH.concat("test resources/");
		cascadeClassifier.load(HAAR_CASCADE_RESOURCE);
		image = Imgcodecs.imread(testPath.concat("me.jpg"));

		MatOfRect faceDetections = new MatOfRect();
		cascadeClassifier.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

		for(Rect rect : faceDetections.toArray())
		{
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}

		String filename = testPath.concat("testOuput.jpg");
		System.out.println(String.format("Writing %s", filename));
		Imgcodecs.imwrite(filename, image);
	}


}
