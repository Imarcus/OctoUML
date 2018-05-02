package model.nodes;

public class Attribute extends IdentifiedTextField {
	public Attribute(String text) {
		super(text);
		setPromptText("-nome_do_atributo:Tipo");
	}
}
