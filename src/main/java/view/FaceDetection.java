
package view;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opencv.core.Core;
import com.sun.istack.internal.logging.Logger;
import controller.Broker;
import controller.FaceDetectionController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import mail.MailSender;

public class FaceDetection extends Application {

	// private static final Queue<Object> queue = new ConcurrentLinkedDeque<>();
	// private void startWork() {
	// try
	// {
	// Broker broker = new Broker();
	//
	// ExecutorService threadPool = Executors.newFixedThreadPool(2);
	//
	// threadPool.execute(new MailSender(broker));
	// Future producerStatus = threadPool.submit(/* new PRODUCER(broker) */);
	//
	// // this will wait for the producer to finish its execution.
	// producerStatus.get();
	//
	// threadPool.shutdown();
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	Logger logger = Logger.getLogger(FaceDetection.class);
	public static final Broker broker = new Broker();


	@Override
	public void start(Stage primaryStage) {

		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FaceDetection.fxml"));
			BorderPane root = (BorderPane)loader.load();
			root.setStyle("-fx-background-color: whitesmoke;");
			// create and style a scene
			Scene scene = new Scene(root, 1200, 800);
			scene.getStylesheets()
					.add(getClass().getResource("application.css")
							.toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.setTitle("Face Detection and Tracking Manager");
			primaryStage.setScene(scene);
			// show the GUI
			primaryStage.show();
			// init the controller
			FaceDetectionController controller = loader.getController();



			ExecutorService threadPool = Executors.newFixedThreadPool(2);
			threadPool.execute(new MailSender(broker));
			// Future producerStatus = threadPool.submit(new Runnable() {
			//
			// @Override
			// public void run() {
			// try
			// {
			// controller.init(broker);
			// System.out.println("FaceDetectionController has been initialized!");
			// }
			// catch(FileNotFoundException e)
			// {
			// logger.info(e.getMessage());
			// e.printStackTrace();
			// }
			// finally
			// {
			// threadPool.shutdown();
			// }
			// }
			// });
			// // this will wait for the producer to finish its execution.
			// producerStatus.get();

			controller.init(broker);
			System.out.println("FaceDetectionController has been initialized!");


		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			primaryStage.setOnCloseRequest(x-> {
				Platform.exit();
				System.exit(0);
			});
		}
	}


	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
