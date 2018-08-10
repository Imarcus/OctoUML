package util;

public class GlobalVariables {

	private static String userName;

	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		GlobalVariables.userName = userName;
	}
}
