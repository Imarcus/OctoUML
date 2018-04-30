package model;

import java.util.UUID;

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
			xmiId = UUID.randomUUID().toString();
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
		return xmiId + SEPARATOR + getText();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IdentifiedTextField) {
			IdentifiedTextField aux = (IdentifiedTextField) obj;
			if (xmiId != null) {
				if (xmiId.equals(aux.getXmiId()) && getText().equals(aux.getText())) {
					return true;
				}
			} else {
				if (getText().equals(aux.getText())) {
					return true;
				}
			}
		}
		return false;
	}
}
