
package utils;

import java.io.IOException;
import sun.misc.BASE64Decoder;

public final class StringUtils {

	public static enum CascadeResources
	{
		LBP(StringUtils.RESOURCES_PATH.concat("lbpcascades/lbpcascade_frontalface.xml")),
		HAAR(StringUtils.RESOURCES_PATH.concat("haarcascades/haarcascade_frontalface_alt.xml")),
		EYES(StringUtils.RESOURCES_PATH.concat("haarcascades/haarcascade_eye_tree_eyeglasses.xml"));

		private String value;


		CascadeResources(final String value){
			this.value = value;
		}


		public String getValue() {
			return value;
		}


		@Override
		public String toString() {
			return this.getValue();
		}
	}

	public static final String RESOURCES_PATH = System.getProperty("user.dir")
			.concat("/resources/");


	/**
	 * Decrypts the string and removes the salt
	 * 
	 * @param encryptedKey
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String encryptedKey) {
		String decrypted = "";
		if(encryptedKey.length() > 12)
		{
			// String cipher = encryptedKey.substring(12);
			try
			{
				decrypted = new String(new BASE64Decoder().decodeBuffer(encryptedKey));
			}
			catch(IOException e)
			{
				System.out.println(e);
			}

		}

		return decrypted;
	}
}
