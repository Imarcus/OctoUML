package util.insertIMG;

/**
 * Used by MainController for fetching a image to be used in the graph.
 */

import java.awt.geom.Point2D;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import controller.AbstractDiagramController;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class InsertIMG {

    ImageView myImageView;
    Stage aStage;
    Pane aDrawPane;
    private AbstractDiagramController controller;
    private Point2D.Double point;

    public InsertIMG(Stage pStage, Pane pDrawPane) {
        aStage = pStage;
        aDrawPane = pDrawPane;
    }

    public void openFileChooser(AbstractDiagramController controller, Point2D.Double point) {
        this.point = point;
        this.controller = controller;

        final FileChooser fileChooser = new FileChooser();

        configureFileChooser(fileChooser);
        File file = fileChooser.showOpenDialog(aStage.getScene().getWindow());
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

            myImageView = new ImageView();
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            myImageView.setImage(image);
            controller.createPictureView(myImageView, image, point);


        } catch (Exception ex) {
            Logger.getLogger(InsertIMG.class.getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }


}
