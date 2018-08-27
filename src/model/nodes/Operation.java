package model.nodes;

public class Operation extends IdentifiedTextField {

	private int index;

	public Operation(String text) {
		super(text);
		setPromptText("+nome_da_operação()");
	}
	
	public Operation(int index, String text) {
		super(text);
		this.index = index;
		setPromptText("+nome_da_operação()");
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}	
}
