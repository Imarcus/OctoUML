package util;

public class GlobalVariables {

	private static String userName;
	private static String colaborationType = Constants.collaborationTypeSynchronous;
	
	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		GlobalVariables.userName = userName;
	}

	public static String getColaborationType() {
		return colaborationType;
	}

	public static void setColaborationType(String colaborationType) {
		GlobalVariables.colaborationType = colaborationType;
	}
}
