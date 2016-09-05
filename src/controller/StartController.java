package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by chalmers on 2016-08-08.
 */
public class StartController {

    @FXML
    Button classDiagramButton;

    @FXML
    Label titleLabel, subTitleLabel;

    TabController tabController;

    @FXML
    public void initialize() {
        Image icon = new Image("/icons/classDiagram.PNG");
        classDiagramButton.setGraphic(new ImageView(icon));
        classDiagramButton.setContentDisplay(ContentDisplay.BOTTOM);

        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 60));
        titleLabel.setTextFill(Color.web("#3F4144"));

        subTitleLabel.setFont(Font.font("Verdana", 30));
    }

    public void handleActionNew(){
        tabController.getTabPane().getTabs().clear();
        tabController.addTab(TabController.CLASS_DIAGRAM_VIEW_PATH);
    }

    public void setTabController(TabController tc){
        tabController = tc;
    }

}
