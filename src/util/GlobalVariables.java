package util;

import java.util.Locale;
import java.util.ResourceBundle;

public class GlobalVariables {
 
	private static Locale locale = Locale.getDefault();
	private static ResourceBundle bundle = ResourceBundle.getBundle("resources/messages", locale);
	
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
