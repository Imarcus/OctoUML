package controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by chalmers on 2016-08-22.
 */
public class GithubRepoDialogController {

    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    public TextField urlTextField, imageNameTextField, commitTextField, xmiNameTextField;

    @FXML
    public CheckBox xmiCheckBox, imageCheckBox;

    @FXML
    public void initialize(){
        imageNameTextField.setDisable(true);
        imageNameTextField.setDisable(true);

    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    public void handleOk() {
        if (isInputValid()) {
            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    public void handleCancel() {
        dialogStage.close();
    }

    //TODO
    private boolean isInputValid(){
        return true;
    }

    /**
     * Sets the stage of this dialog.
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    public void onImageCheckBox(){
        if(imageCheckBox.isSelected()){
            imageNameTextField.setDisable(false);
        } else {
            imageNameTextField.setDisable(true);
        }
    }

    public void onXmiCheckBox(){
        if(xmiCheckBox.isSelected()){
            xmiNameTextField.setDisable(false);
        } else {
            xmiNameTextField.setDisable(true);
        }
    }

}
