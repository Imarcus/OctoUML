package util;

import java.util.Locale;
import java.util.ResourceBundle;

public class GlobalVariables {
 
	private static String userName;
	private static String collaborationType = Constants.collaborationTypeSynchronous;
	private static Locale locale = Locale.getDefault();
	private static ResourceBundle bundle = ResourceBundle.getBundle("resources/messages", locale);
	
	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String newValue) {
		userName = newValue;
	}

	public static String getCollaborationType() {
		return collaborationType;
	}

	public static void setCollaborationType(String newValue) {
		collaborationType = newValue;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		GlobalVariables.locale = locale;
	}
	
	public static String getString(String key) {
		return bundle.getString(key);
	}
}
