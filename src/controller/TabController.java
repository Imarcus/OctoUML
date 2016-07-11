package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chalmers on 2016-07-07.
 */
public class TabController {

    @FXML
    private CheckMenuItem umlMenuItem, sketchesMenuItem, mouseMenuItem, gridMenuItem;

    @FXML
    private TabPane tabPane;

    private Stage stage;

    private Map<Tab, MainController> tabMap = new HashMap<>();


    @FXML
    public void initialize() {
    }

    public TabPane getTabPane(){
        return tabPane;
    }

    public void setStage(Stage pStage){
        stage = pStage;
    }

    public void addTab(){
        StackPane canvasView = null; //TODO FIX
        MainController mainController = null;
        FXMLLoader loader;

        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
            canvasView = (StackPane) loader.load();
            mainController = (MainController) loader.getController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Tab tab = new Tab();
        tab.setContent(canvasView);
        tabMap.put(tab, mainController);
        tab.setText("Tab " + tabMap.size());

        tabPane.getTabs().add(tab);
        mainController.setStage(stage);
    }

    public void handleMenuActionUML() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionUML();
    }
    public void handleMenuActionSketches() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSketches();
    }
    public void handleMenuActionGrid() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionGrid();
    }
    public void handleMenuActionMouse() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionMouse();
    }
    public void handleMenuActionExit() {
    }
    public void handleMenuActionSave() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSave();
    }
    public void handleMenuActionLoad() {
        addTab();
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionLoad();
    }
    public void handleMenuActionNew() {
        addTab();
    }

}
