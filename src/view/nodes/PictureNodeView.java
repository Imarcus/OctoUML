package view.nodes;

import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import model.nodes.PictureNode;
import util.Constants;

import java.beans.PropertyChangeEvent;


/**
 * Visual representation of PictureView.
 */
public class PictureNodeView extends AbstractNodeView {

    private double x;
    private double y;
    private double width;
    private double height;
    private ImageView imageView;

    private Line shortHandleLine;
    private Line longHandleLine;

    public PictureNodeView(ImageView v, PictureNode picnode) {
        super(picnode);
        imageView = v;
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        getChildren().add(v);
        createHandles();
    }

    @Override
    public void setFill(Paint p) {

    }

    public void setX(double x){
        this.x = x;
        setTranslateX(x);
    }

    public void setY(double y) {
        this.y = y;
        setTranslateY(y);
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public Bounds getBounds() {
        return imageView.getBoundsInParent();
    }

    public void changeHeight(Double newHeight){
        imageView.setFitHeight(newHeight);
        //imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        setHeight(imageView.getFitHeight());

        shortHandleLine.setStartY(this.getHeight()-3);
        shortHandleLine.setEndY(this.getHeight()-7);
        longHandleLine.setStartY(this.getHeight()-3);
        longHandleLine.setEndY(this.getHeight()-15);
    }

    public void changeWidth(Double newWidth){
        imageView.setFitWidth(newWidth);
        //imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        setWidth(imageView.getFitWidth());

        shortHandleLine.setStartX(this.getWidth()-7);
        shortHandleLine.setEndX(this.getWidth()-3);
        longHandleLine.setStartX(this.getWidth()-15);
        longHandleLine.setEndX(this.getWidth()-3);
    }

    private void createHandles(){

        shortHandleLine = new Line(this.getWidth()-7,this.getHeight()-3, this.getWidth()-3, this.getHeight()-7);
        longHandleLine = new Line(this.getWidth()-15,this.getHeight()-3, this.getWidth()-3, this.getHeight()-15);

        this.getChildren().addAll(shortHandleLine, longHandleLine);
    }


    public void setSelected(boolean selected) {
        if(selected){
            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,109,112,0.8), 10, 0, 0, 0);");
        } else {
            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (evt.getPropertyName().equals(Constants.changeNodeX)) {
            setX((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeY)) {
            setY((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeWidth)) {
            changeWidth((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeHeight)) {
            changeHeight((double) evt.getNewValue());
        }
    }

}
