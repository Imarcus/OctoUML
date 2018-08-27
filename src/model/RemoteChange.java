package model;

import java.util.Date;

public class RemoteChange {

	private String userName;
	private Date date;
	private Object change;

	public RemoteChange(String userName, Date date, Object change) {
		super();
		this.userName = userName;
		this.date = date;
		this.change = change;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Object getChange() {
		return change;
	}
	public void setChange(Object change) {
		this.change = change;
	}
}
