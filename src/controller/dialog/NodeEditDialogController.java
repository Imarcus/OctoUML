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

        /*String attributes = "";
        String operations = "";

        //Create one string with newlines after each string
        for(String att : node.attributesProperty()){
            attributes += att + "\n";
        }

        for(String op : node.operationsProperty()){
            operations += op + "\n";
        }*/

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

    private ArrayList<String> getStringsFromArea(TextArea area){
        String areaText = area.getText();
        if(areaText == null){
            return null;
        }
        ArrayList<String> arraylist = new ArrayList<>();
        Scanner scanner = new Scanner(areaText);
        while (scanner.hasNextLine()) {
            arraylist.add(scanner.nextLine());
            // process the line
        }
        scanner.close();
        return arraylist;
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
        return true;
        /*String errorMessage = "";

        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (streetField.getText() == null || streetField.getText().length() == 0) {
            errorMessage += "No valid street!\n";
        }

        if (postalCodeField.getText() == null || postalCodeField.getText().length() == 0) {
            errorMessage += "No valid postal code!\n";
        } else {
            // try to parse the postal code into an int
            try {
                Integer.parseInt(postalCodeField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid postal code (must be an integer)!\n";
            }
        }

        if (cityField.getText() == null || cityField.getText().length() == 0) {
            errorMessage += "No valid city!\n";
        }

        if (birthdayField.getText() == null || birthdayField.getText().length() == 0) {
            errorMessage += "No valid birthday!\n";
        } else {
            if (!CalendarUtil.validString(birthdayField.getText())) {
                errorMessage += "No valid birthday. Use the format yyyy-mm-dd!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message
            Dialogs.showErrorDialog(dialogStage, errorMessage,
                    "Please correct invalid fields", "Invalid Fields");
            return false;
        }*/
    }
}