
package mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.sun.istack.internal.logging.Logger;
import utils.StringUtils;

public class Mail {

	private String mailFrom;
	private String password;
	private String mailTo;
	private String subject;
	private String text;
	Logger logger = Logger.getLogger(Mail.class);
	private Session session;


	public Mail(String mailFrom, String password, String mailTo, String subject, String text){
		setMailFrom(mailFrom);
		setPassword(StringUtils.decrypt(password));
		setMailTo(mailTo);
		setSubject(subject);
		setText(text);
		session = Session.getDefaultInstance(getProperties(), new javax.mail.Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				logger.info("The server is authenticating you now...");
				return new PasswordAuthentication(getMailFrom(), getPassword());
			}
		});
		session.setDebug(true);
	}


	private String getMailFrom() {
		return this.mailFrom;
	}


	private String getPassword() {
		return this.password;
	}


	private String getMailTo() {
		return this.mailTo;
	}


	private String getSubject() {
		return this.subject;
	}


	private String getText() {
		return this.text;
	}


	private void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}


	private void setPassword(String password) {
		this.password = password;
	}


	private void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}


	private void setSubject(String subject) {
		this.subject = subject;
	}


	private void setText(String text) {
		this.text = text + '\n';
	}


	private Properties getProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp." + getMailFrom().split("\\@")[1]);
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "465");
		// properties.put("mail.smtp.auth", "true");
		return properties;
	}


	public void sendWithAttachment(String attachment) {
		try
		{
			MimeMessage message = new MimeMessage(session);
			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
			message.addHeader("format", "flowed");
			message.addHeader("Content-Transfer-Encoding", "16bit");
			message.setFrom(new InternetAddress(getMailFrom(), "DevNULL" /* getMailFrom().split("\\@")[0] */));
			message.setReplyTo(InternetAddress.parse(getMailFrom(), false));
			message.setSubject(getSubject(), "UTF-8");
			message.setSentDate(new Date());
			message.setText(getText());
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getMailTo(), false));

			// Create the message body part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(getText().concat(System.lineSeparator()));

			// Create a multipart message for attachment
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Second part is image attachment
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(attachment);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(attachment);
			// Trick is to add the content-id header here
			messageBodyPart.setHeader("Content-ID", "image_id");
			multipart.addBodyPart(messageBodyPart);

			// Set the multipart message to the email message
			message.setContent(multipart);

			Transport.send(message);
			System.out.println("Done!");
		}
		catch(MessagingException | UnsupportedEncodingException e)
		{
			System.err.println(e);
			throw new RuntimeException("Uncaught exception: " + e);
		}
	}



	public void sendWithAttachment(List<String> attachments) {
		try
		{
			MimeMessage message = new MimeMessage(session);
			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
			message.addHeader("format", "flowed");
			message.addHeader("Content-Transfer-Encoding", "16bit");
			message.setFrom(new InternetAddress(getMailFrom(), "DevNULL" /* getMailFrom().split("\\@")[0] */));
			message.setReplyTo(InternetAddress.parse(getMailFrom(), false));
			message.setSubject(getSubject(), "UTF-8");
			message.setSentDate(new Date());
			message.setText(getText());
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getMailTo(), false));

			// Create the message body part
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(getText().concat(System.lineSeparator()));

			// // Create a multipart message for attachment
			// Multipart multipart = new MimeMultipart();
			//
			// // Set text message part
			// multipart.addBodyPart(messageBodyPart);

			for(String attachment : attachments)
			{
				// Create a multipart message for attachment
				Multipart multipart = new MimeMultipart();
				// Set text message part
				multipart.addBodyPart(messageBodyPart);
				// Second part is image attachment
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource("images/" + attachment + ".png");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(attachment + ".png");
				// Trick is to add the content-id header here
				messageBodyPart.setHeader("Content-ID", "image_id");
				multipart.addBodyPart(messageBodyPart);
				// Set the multipart message to the email message
				message.setContent(multipart);
			}



			Transport.send(message);
			System.out.println("Done!");
		}
		catch(MessagingException | UnsupportedEncodingException e)
		{
			System.err.println(e);
			throw new RuntimeException("Uncaught exception: " + e);
		}
	}
}
