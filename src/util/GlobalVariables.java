package util;

import java.util.Enumeration;
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
	
	public static String getKey(String value) {
		String result = "";
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String aux = bundle.getString(key);
			if (aux.equals(value)) {
				result = key;
			}
		}
		return result;
	}	
}
