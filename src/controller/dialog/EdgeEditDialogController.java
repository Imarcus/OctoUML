package controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.edges.AbstractEdge;

/**
 * Dialog to edit Edge settings.
 *
 * @author Marco Jakob
 */
public class EdgeEditDialogController {

    @FXML
    private ChoiceBox directionBox;
    @FXML
    private ChoiceBox typeBox;
    @FXML
    private TextField startMultiplicity;
    @FXML
    private TextField endMultiplicity;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private AbstractEdge edge;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the classDiagramView.fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }

    public String getStartMultiplicity() {
        return startMultiplicity.getText();
    }

    public String getEndMultiplicity() {
        return endMultiplicity.getText();
    }

    public Button getOkButton() {
        return okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public ChoiceBox getDirectionBox() {
        return directionBox;
    }

    public ChoiceBox getTypeBox() {
        return typeBox;
    }

    public void setEdge(AbstractEdge edge) {
        this.edge = edge;
        //TODO Hardcoded values. Where to put them?
        typeBox.getItems().setAll("Association", "Inheritance", "Aggregation", "Composition");
        typeBox.getSelectionModel().select(edge.getType());
        directionBox.getItems().setAll(AbstractEdge.Direction.values());
        directionBox.getSelectionModel().select(edge.getDirection());
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
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        return true; //Use if we want to
    }
}