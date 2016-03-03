package controller.dialog;

/**
 * Created by marcusisaksson on 2016-02-25.
 */
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.ClassNode;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Dialog to edit details of a person.
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


    private Stage dialogStage;
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
     * Sets the stage of this dialog.
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the person to be edited in the dialog.
     *
     * @param person
     */
    public void setNode(ClassNode person) {
        this.node = person;

        titleField.setText(node.getTitle());
        attributesArea.setText(node.attributesProperty().getValue());
        operationsArea.setText(node.operationsProperty().getValue());
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
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            node.setTitle(titleField.getText());
            node.setAttributes(attributesArea.getText());
            node.setOperations(operationsArea.getText());

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
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