package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Launches application with main view "tabView.fxml"
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
