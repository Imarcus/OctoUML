package controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.nodes.ClassNode;

/**
 * Dialog to edit details of a node.
 *
 * @author Marco Jakob
 */
public class NodeEditDialogController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea attributesArea;
    @FXML
    private TextArea operationsArea;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;


    private ClassNode node;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the classDiagramView.fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }


    /**
     * Sets the node to be edited in the dialog.
     *
     * @param node
     */
    public void setNode(ClassNode node) {
        this.node = node;

        titleField.setText(this.node.getTitle());
        attributesArea.setText(this.node.getAttributes());
        operationsArea.setText(this.node.getOperations());
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getAttributes(){
        return attributesArea.getText();
    }

    public String getOperations() {
        return operationsArea.getText();
    }

    public boolean hasTitledChanged(){
        if(this.node.getTitle() == null){
            return titleField.getText() != null;
        } else {
            return !this.node.getTitle().equals(titleField.getText());
        }
    }

    public boolean hasAttributesChanged(){
        if(this.node.getAttributes() == null){
            return attributesArea.getText() != null;
        } else {
            return !this.node.getAttributes().equals(attributesArea.getText());
        }
    }

    public boolean hasOperationsChanged(){
        if(this.node.getOperations() == null){
            return operationsArea.getText() != null;
        } else {
            return !this.node.getOperations().equals(operationsArea.getText());
        }
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    private void handleOk() {
        if (isInputValid()) {
            okClicked = true;
        }
    }

    /**
     * Called when the user clicks cancel.
     */

    private void handleCancel() {
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        return true; //Use if we want to
    }
}