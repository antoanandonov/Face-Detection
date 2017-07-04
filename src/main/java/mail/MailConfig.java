
package mail;

import java.lang.reflect.Type;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import utils.StringUtils;

public class MailConfig {

	public static final Type REVIEW_TYPE = new TypeToken<List<MailConfig>>() {
	}.getType();
	public static final String JSON_PATH = StringUtils.RESOURCES_PATH.concat("conf.json");
	private String mailFrom;
	private String password;
	private String mailTo;
	private String subject;
	private String content;


	private MailConfig(){}


	public String getMailFrom() {
		return mailFrom;
	}


	public String getPassword() {
		return password.substring(password.lastIndexOf('=') + 1);
	}


	public String getMailTo() {
		return mailTo;
	}


	public String getSubject() {
		return subject;
	}


	public String getContent() {
		return content;
	}
}
