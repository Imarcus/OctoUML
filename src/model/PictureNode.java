package model;
import javafx.scene.image.Image;

/**
 * Represents an image in the graph.
 */
public class PictureNode extends AbstractNode{

    private  Image image ;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }


    @Override
    public AbstractNode copy() {
        return null;
    } //TODO


    public PictureNode (Image image, double x, double y, double width, double height)
    {
        super(x, y, width, height );
        this.image= image ;
    }
}
