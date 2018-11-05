package model.nodes;

import util.GlobalVariables;

public class Attribute extends IdentifiedTextField {

	public Attribute(String text) {
		super(text);
		setPromptText(GlobalVariables.getString("typeIt")
				+ ": - " + GlobalVariables.getString("name")
				+ " : " + GlobalVariables.getString("type"));
	}	
}
