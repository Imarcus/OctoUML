package controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GithubLoginDialogController {
    private Stage dialogStage;
    private boolean okClicked = false;

    @FXML
    public TextField nameTextField;

    @FXML
    public PasswordField passwordField;

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
}
