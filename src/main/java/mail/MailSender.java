
package mail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import org.opencv.core.Mat;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.istack.internal.logging.Logger;
import controller.Broker;

public final class MailSender implements Runnable {

	private Broker broker;
	private JsonReader jsonReader;
	private List<MailConfig> jsonData;
	private Mail mail;
	Logger logger = Logger.getLogger(MailSender.class);
	private static Long lastTimeMailSent = System.currentTimeMillis();


	private MailSender(){}


	public MailSender(Broker broker) throws FileNotFoundException{
		this.jsonReader = new JsonReader(new FileReader(MailConfig.JSON_PATH));
		this.jsonData = new Gson().fromJson(jsonReader, MailConfig.REVIEW_TYPE);
		this.broker = broker;
		this.mail = initMailObject();
	}


	private Mail initMailObject() {
		MailConfig mailConfig = jsonData.get(0);
		Mail mail = new Mail(mailConfig.getMailFrom(), mailConfig.getPassword(), mailConfig.getMailTo(), mailConfig.getSubject(), mailConfig.getContent());
		return mail;
	}


	// public static void sendMail(Broker broker) {
	// try
	// {
	// new Thread(new MailSender(broker)).start();
	// }
	// catch(FileNotFoundException e)
	// {
	// e.printStackTrace();
	// }
	// }


	@Override
	public void run() {
		System.out.println(System.lineSeparator() + "RUNNING THREAD -> " + Thread.currentThread()
				.getName() + " with id: " + Thread.currentThread()
						.getId());
		try
		{
			Mat data;
			// Mat data = broker.get();
			// while(broker.continueProducing || data != null)
			while(true)
			{
				Thread.sleep(10_000);
				List<String> images = broker.getFiveImages();
				if( !images.isEmpty())
				{
					mail.sendWithAttachment(images);
				}

				// data = broker.get();
				// System.out.println("Consumer [" + this + "] processed data from broker: " + data);
				// if(data != null)
				{
					// must save all photos in collection and send it via mail
					// FaceDetectorUtils.saveMatТоImage(data);
					// mail.sendWithAttachment("images/" + FaceDetectorUtils.saveMatТоImage(data) + ".png");
				}
			}
		}
		catch(InterruptedException e)
		{
			logger.info(e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Comsumer " + this + " finished its job.");
		}

	}
}
