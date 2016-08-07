package controller;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The class controlling the top menu and the tabs.
 */
public class TabController {

    @FXML
    private CheckMenuItem umlMenuItem, sketchesMenuItem, mouseMenuItem, gridMenuItem, snapToGridMenuItem, snapIndicatorsMenuItem;

    @FXML
    Pane content;

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

    public Tab addTab(){
        BorderPane canvasView = null;
        MainController mainController = null;
        FXMLLoader loader;

        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
            canvasView = loader.load();
            mainController = loader.getController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Tab tab = new Tab();

        tab.setContent(canvasView);
        tabMap.put(tab, mainController);
        tab.setText("Diagram " + tabMap.size());

        tabPane.getTabs().add(tab);
        mainController.setStage(stage);


        return tab;
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
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        tabMap.get(tab).handleMenuActionLoad();
        tab.setText(tabMap.get(tab).getGraphModel().getName());
    }
    public void handleMenuActionNew() {
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
    }

    public void handleMenuActionServer(){
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionServer();
    }

    public void handleMenuActionClient(){
        Tab tab = addTab();
        tabPane.getSelectionModel().select(tab);
        if(!tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionClient()){
            tabPane.getTabs().remove(tab);
        }
    }

    public void handleMenuActionImage(){
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionImage();
    }

    public void handleMenuActionSnapToGrid() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSnapToGrid(snapToGridMenuItem.isSelected());
    }

    public void handleMenuActionSnapIndicators() {
        tabMap.get(tabPane.getSelectionModel().getSelectedItem()).handleMenuActionSnapIndicators(snapIndicatorsMenuItem.isSelected());
    }
}
