package util;

public class Change {
	
	// Constants
    public static String delete = "delete";
    public static String remoteUpdateWithLocalDelete = "remoteUpdateWithLocalDelete";
    public static String newElement = "newElement";
    
    public static String visibility = "visibility";
    public static String name = "name";
    public static String arguments = "arguments";
    public static String type = "type";
    public static String moved = "moved";
	
	private String changeType;
	private Object change;
	
	public Change(String changeType, Object change) {
		this.changeType = changeType;
		this.change = change;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public Object getChange() {
		return change;
	}

	public void setChange(Object change) {
		this.change = change;
	}
}
