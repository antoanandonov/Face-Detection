
package mail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import javax.mail.Message;
import javax.mail.Transport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import utils.FaceDetectorUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Transport.class, FaceDetectorUtils.class})
public class MailSenderTest {

	private static JsonReader jsonReader;
	private static List<MailConfig> jsonData;
	private static Mail mail;


	@BeforeClass
	public static void init() throws FileNotFoundException {
		jsonReader = new JsonReader(new FileReader(MailConfig.JSON_PATH));
		jsonData = new Gson().fromJson(jsonReader, MailConfig.REVIEW_TYPE);

		mail = initMailObject();
	}


	private static Mail initMailObject() {
		MailConfig mailConfig = jsonData.get(0);
		Mail mail = new Mail(mailConfig.getMailFrom(), mailConfig.getPassword(), mailConfig.getMailTo(), mailConfig.getSubject(), mailConfig.getContent());
		return mail;
	}


	@Test(timeout = 10_000)
	public void testSendWithAttachment() throws Exception {

		PowerMockito.mockStatic(Transport.class);
		PowerMockito.doNothing()
				.when(Transport.class, "send", Mockito.any(Message.class));

		mail.sendWithAttachment("resources/test resources/me.jpg");
	}


	// @Test
	// public void testGetFromQueue() throws Exception {
	// Broker broker = Mockito.spy(new Broker());
	// broker.queue.add(new Mat());
	//
	// Mockito.doReturn(new Mat())
	// .when(broker)
	// .get();
	//
	// PowerMockito.mockStatic(FaceDetectorUtils.class);
	// PowerMockito.doReturn("")
	// .when(FaceDetectorUtils.class, "saveMatТоImage", Mockito.any(Mat.class));
	//
	// Thread thread = new Thread(new MailSender(broker));
	// thread.start();
	//
	// }


	@AfterClass
	public static void clean() {
		jsonReader = null;
		jsonData = null;
		mail = null;
	}
}
