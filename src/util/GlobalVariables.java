package util;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {
 
	private static String userName;
	private static String collaborationType = Constants.collaborationTypeSynchronous;
	
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
}
