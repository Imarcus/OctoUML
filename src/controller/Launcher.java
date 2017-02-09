package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Launches application with main view "tabView.classDiagramView.fxml"
 */
public class Launcher extends Application {

    private TabController tabController = null;

    public void start(Stage stage) {
        VBox tabView = null;
        FXMLLoader loader;
        BorderPane startView = null;
        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/mainView.fxml"));
            tabView = loader.load();
            tabController = loader.getController();
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/startView.fxml"));
            startView = loader.load();
            ((StartController)loader.getController()).setTabController(tabController);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Scene scene = new Scene(tabView, 1000, 800);
        tabController.setStage(stage);

        stage.setScene(scene);
        stage.setTitle("OctoUML");
        stage.setFullScreen(true);
        stage.getIcons().add(new Image("icons/appIcon.png"));
        stage.show();

        Tab tab = new Tab();
        tab.setContent(startView);
        tab.setText("Start");
        tabController.getTabPane().getTabs().add(tab);
    }

    public void stop(){
        System.out.println("Stopping!");
        tabController.stop();
    }
    
    public static void main(String args[] ){
    	Launcher.launch(args);
    }
}
