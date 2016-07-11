package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by marcusisaksson on 2016-02-16.
 */
public class Launcher extends Application {

    public void start(Stage stage) throws IOException { //TODO HANDLE EXCEPTION
        VBox tabView = null;
        FXMLLoader loader;
        TabController tabController = null;
        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("tabView.fxml"));
            tabView = loader.load();
            tabController = loader.getController();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        StackPane canvasView = null; //TODO FIX
        MainController mainController = null;
        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
            canvasView = (StackPane) loader.load();
            mainController = (MainController) loader.getController();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Scene scene = new Scene(tabView, 1000, 800);
        //tabView.prefHeightProperty().bind(scene.heightProperty());
        //tabView.prefWidthProperty().bind(scene.widthProperty());
        tabController.setStage(stage);

        stage.setScene(scene);
        stage.setTitle("Penguin");
        //stage.setFullScreen(true);
        stage.show();
    }
}
