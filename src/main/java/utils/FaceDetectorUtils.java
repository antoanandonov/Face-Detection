
package utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import javafx.scene.image.Image;

public final class FaceDetectorUtils {


	public static String saveMatТоImage(Mat image) throws InterruptedException {
		final String faceImage = "face_" + System.currentTimeMillis();
		Imgcodecs.imwrite("images/" + faceImage + ".png", image);
		return faceImage;
	}


	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 *
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	public static Image convertMatToImage(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}


	public static BufferedImage convertMatToBufferedImage(Mat frame) {
		// Mat() to BufferedImage
		int type = 0;
		if(frame.channels() == 1)
		{
			type = BufferedImage.TYPE_BYTE_GRAY;
		}
		else if(frame.channels() == 3)
		{
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		WritableRaster raster = image.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte)raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}


	public static void saveImage(BufferedImage img) {
		try
		{
			final String faceImage = "face_" + System.currentTimeMillis();
			File outputfile = new File("images/" + faceImage + ".png");
			ImageIO.write(img, "png", outputfile);
		}
		catch(Exception e)
		{
			System.out.println("error");
			System.out.println(e);
		}
	}
}
