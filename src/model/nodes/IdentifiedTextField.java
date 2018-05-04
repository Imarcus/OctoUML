package model.nodes;

import javafx.scene.control.TextField;

public class IdentifiedTextField extends TextField {
	
	public static final String SEPARATOR = "|";
	
	private String xmiId;
	
	public IdentifiedTextField(String text) {
		int index;
		
		if (!text.isEmpty()) {
			// Remove index of VBox 
			index = text.indexOf(";");	
			if (index != -1) {
				text = text.substring(index+1);
			}			
			// Split xmiId and text			
			index = text.indexOf(SEPARATOR);
			if (index != -1) {
				xmiId = text.substring(0, index);
				setText(text.substring(index+1));
			} else {
				setText(text);
			}
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
