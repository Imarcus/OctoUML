package model.nodes;

import util.GlobalVariables;

public class Operation extends IdentifiedTextField {

	public Operation(String text) {
		super(text);
		setPromptText(GlobalVariables.getString("typeIt")
				+ ": + " + GlobalVariables.getString("name")
				+ "(" + GlobalVariables.getString("arguments") + ")"
				+ " : " + GlobalVariables.getString("type"));
	}
}
