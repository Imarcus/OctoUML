package model.nodes;

public class Attribute extends IdentifiedTextField {

	private int index;

	public Attribute(String text) {
		super(text);
		setPromptText("-nome_do_atributo:Tipo");
	}	
	
	public Attribute(int index, String text) {
		super(text);
		this.index = index;
		setPromptText("-nome_do_atributo:Tipo");
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
