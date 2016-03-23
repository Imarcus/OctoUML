package controller;

import com.guigarage.flatterfx.FlatterFX;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by marcusisaksson on 2016-02-16.
 */
public class Launcher extends Application {

    public void start(Stage stage) throws IOException { //TODO HANDLE EXCEPTION
        StackPane root = null; //TODO FIX
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view.fxml"));
            root = (StackPane) loader.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        MainController mainController = (MainController) loader.getController();
        mainController.setStage(stage);
        stage.setScene(new Scene(root, 1000, 800));
        stage.setTitle("Penguin");
        stage.show();
        //FlatterFX.style();
    }
}
