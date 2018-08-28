package model.nodes;

public class Attribute extends IdentifiedTextField {

	public Attribute(String text) {
		super(text);
		setPromptText("-nome_do_atributo:Tipo");
	}	
	
	public String toString() {
		return getXmiId() + "|" + getText();
	}
	
	public void toString(String value) {
		setXmiId(value.substring(0, value.indexOf("|")));
		setText(value.substring(value.indexOf("|")+1));
	}
}
