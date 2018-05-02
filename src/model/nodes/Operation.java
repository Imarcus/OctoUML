package model.nodes;

public class Operation extends IdentifiedTextField {
	public Operation(String text) {
		super(text);
		setPromptText("+nome_da_operação()");
	}
}
