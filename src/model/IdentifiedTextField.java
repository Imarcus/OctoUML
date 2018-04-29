package model;

import javafx.scene.control.TextField;

public class IdentifiedTextField extends TextField {
	
	public static final String SEPARATOR = "|";
	
	private String xmiId;
	
	public IdentifiedTextField(String text) {
		int ind = text.indexOf(SEPARATOR);
		if (ind != -1) {
			xmiId = text.substring(0, ind);
			setText(text.substring(ind+1));
		} else {
			setText(text);
		}
	}

	public String getXmiId() {
		return xmiId;
	}

	public void setXmiId(String xmiId) {
		this.xmiId = xmiId;
	}
	
	public String toString() {
		return xmiId + "SEPARATOR" + getText();
	}
	
	@Override
	public boolean equals(Object obj) {
		IdentifiedTextField aux = (IdentifiedTextField) obj;
		if (xmiId.equals(aux.getXmiId()) && xmiId.equals(aux.getText())) {
			return true;
		}
		return false;
	}
}
