package model.nodes;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        Background background =  new Background(backgroundFill);
    	setPadding(new Insets(0));
    	setBackground(background);
    	setFont(Font.font("Verdana", 10));
		setStyle("-fx-prompt-text-fill: white");    	
	}

	public String getXmiId() {
		return xmiId;
	}

	public void setXmiId(String xmiId) {
		this.xmiId = xmiId;
	}
	
    // Make UUIDs short for log purposes
    private String getShortXmiId() {
    	String leftside, rightside;
    	
    	if (xmiId.contains("-")) {
        	leftside = xmiId.substring(0, xmiId.indexOf("-")-4);
        	rightside = xmiId.substring(xmiId.indexOf("-")+24);
    	} else {
    		return xmiId;
    	}
    	return leftside + ".." + rightside;    		
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

	public String toStringShort() {
		return getShortXmiId() + SEPARATOR + getText();
	}
	
	public String toString() {
		return getXmiId() + "|" + getText();
	}
	
	public IdentifiedTextField toString(String value) {
		setXmiId(value.substring(0, value.indexOf("|")));
		setText(value.substring(value.indexOf("|")+1));
		return this;
	}
}
