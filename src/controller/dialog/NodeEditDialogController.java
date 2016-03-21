package controller.dialog;

/**
 * Created by marcusisaksson on 2016-02-25.
 */
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import model.ClassNode;

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
     * after the fxml file has been loaded.
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
        attributesArea.setText(this.node.attributesProperty().getValue());
        operationsArea.setText(this.node.operationsProperty().getValue());
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