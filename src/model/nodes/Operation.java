package model.nodes;

public class Operation extends IdentifiedTextField {

	public Operation(String text) {
		super(text);
		setPromptText("+nome_da_operação()");
	}
	
	public String toString() {
		return getXmiId() + "|" + getText();
	}
	
	public void toString(String value) {
		setXmiId(value.substring(0, value.indexOf("|")));
		setText(value.substring(value.indexOf("|")+1));
	}
}
