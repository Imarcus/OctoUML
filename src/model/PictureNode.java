package model;
import javafx.scene.image.Image;

/**
 * Created by anasm on 2016-06-28.
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
    }


    public PictureNode (Image image, double x, double y, double width, double height)
    {
        super(x, y, width, height );
        this.image= image ;
    }
}
