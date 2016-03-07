package controller.dialog;

/**
 * Created by marcusisaksson on 2016-02-25.
 */
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.AssociationEdge;
import model.ClassNode;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Dialog to edit details of a person.
 *
 * @author Marco Jakob
 */
public class EdgeEditDialogController {

    @FXML
    private CheckBox navigableCheckBox;
    @FXML
    private TextField startMultiplicity;
    @FXML
    private TextField endMultiplicity;


    private Stage dialogStage;
    private AssociationEdge edge;
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


    public void setEdge(AssociationEdge edge) {
        this.edge = edge;
        navigableCheckBox.setSelected(edge.isNavigable());
        startMultiplicity.setText(edge.getStartMultiplicity());
        endMultiplicity.setText(edge.getEndMultiplicity());
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
            edge.setNavigable(navigableCheckBox.isSelected());
            edge.setStartMultiplicity(startMultiplicity.getText());
            edge.setEndMultiplicity(endMultiplicity.getText());

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