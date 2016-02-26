package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by marcusisaksson on 2016-02-16.
 */
public class Launcher extends Application {

    public void start(Stage stage) throws IOException { //TODO HANDLE EXCEPTION
        BorderPane root = null; //TODO FIX
        try {
            root = (BorderPane) FXMLLoader.load(getClass().getClassLoader().getResource("view.fxml"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        stage.setScene(new Scene(root, 1000, 800));
        stage.setTitle("Penguin");
        stage.show();
    }
}
