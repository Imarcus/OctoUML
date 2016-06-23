package util.insertIMG;

/**
 * Created by chalmers on 2016-06-23.
 */
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ecologylab.standalone.researchnotebook.compositionTS.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InsertIMG  {

    private Desktop desktop = Desktop.getDesktop();
    Stage aStage;
    Pane aDrawPane;

    public InsertIMG (Stage pStage, Pane pDrawPane){
        aStage = pStage;
        aDrawPane = pDrawPane;
    }

    public void openFileChooser(){
        final FileChooser fileChooser = new FileChooser();

        configureFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(aStage);
        if (file != null) {
            openFile(file);
        }
    }

    private static void configureFileChooser(
            final FileChooser fileChooser) {
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
    }

    private void openFile(File file) {
        try {
            desktop.open(file);


           aDrawPane.getChildren().add(ImageView.class.cast(file));

        } catch (IOException ex) {
            Logger.getLogger(InsertIMG.class.getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }
}
