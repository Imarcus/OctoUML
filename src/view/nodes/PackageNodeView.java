package view.nodes;


import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.nodes.PackageNode;
import util.Constants;

import java.beans.PropertyChangeEvent;

/**
 * Visual representation of PackageNode-class.
 */
public class PackageNodeView extends AbstractNodeView {

    private PackageNode refNode;
    private Text title;
    private VBox container;
    private StackPane bodyStackPane;
    private Rectangle top;
    private Rectangle body;
    private final double TOP_HEIGHT_RATIO = 0.2;
    private final double TOP_WIDTH_RATIO = 0.4;
    private final double TOP_MAX_HEIGHT = 30;
    private final double TOP_MAX_WIDTH = 150;

    Line shortHandleLine;
    Line longHandleLine;

    public PackageNodeView(PackageNode node) {
        super(node);
        refNode = node;
        title = new Text(node.getTitle());
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        //TODO Ugly solution, hardcoded value.
        title.setWrappingWidth(node.getWidth() - 7);
        container = new VBox();
        bodyStackPane = new StackPane();

        container.setSpacing(0);

        createRectangles();


        container.getChildren().add(top);
        bodyStackPane.getChildren().addAll(body, title);
        container.getChildren().addAll(bodyStackPane);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(top, Pos.CENTER_LEFT);
        setTitleSize();
        this.getChildren().add(container);
        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());

        createHandles();
    }

    private void setTitleSize(){
        title.resize(top.getHeight(), top.getWidth());
    }

    private void createRectangles(){
        top = new Rectangle();
        body = new Rectangle();
        changeHeight(getRefNode().getHeight());
        changeWidth(getRefNode().getWidth());

        top.setX(getRefNode().getX());
        top.setY(getRefNode().getY());
        top.setFill(Color.LIGHTSKYBLUE);
        top.setStroke(Color.BLACK);

        body.setX(getRefNode().getX());
        body.setY(getRefNode().getY() + top.getHeight());
        body.setFill(Color.LIGHTSKYBLUE);
        body.setStroke(Color.BLACK);

    }

    private void createHandles(){

        shortHandleLine = new Line();
        longHandleLine = new Line();

        shortHandleLine.startXProperty().bind(body.widthProperty().subtract(7));
        shortHandleLine.startYProperty().bind(body.heightProperty().add(top.heightProperty().subtract(3)));
        shortHandleLine.endXProperty().bind(body.widthProperty().subtract(3));
        shortHandleLine.endYProperty().bind(body.heightProperty().add(top.heightProperty().subtract(7)));
        longHandleLine.startXProperty().bind(body.widthProperty().subtract(15));
        longHandleLine.startYProperty().bind(body.heightProperty().add(top.heightProperty().subtract(3)));
        longHandleLine.endXProperty().bind(body.widthProperty().subtract(3));
        longHandleLine.endYProperty().bind(body.heightProperty().add(top.heightProperty().subtract(15)));

        this.getChildren().addAll(shortHandleLine, longHandleLine);
    }

    @Override
    public boolean contains(double x, double y) {
        //If there is a childNode inside this PackageNode, we should return false.
        if (refNode.findNode(new Point2D(x, y)) != null) {
            return false;
        }
        return super.contains(x, y);
    }

    public void setStrokeWidth(double scale) {
        top.setStrokeWidth(scale);
        body.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
        top.setFill(p);
        body.setFill(p);
    }

    public void setStroke(Paint p) {
        top.setStroke(p);
        body.setStroke(p);
    }

    public void setSelected(boolean selected){
        if(selected){
            setStroke(Constants.selected_color);
            setStrokeWidth(2);
        } else {
            setStroke(Color.BLACK);
            setStrokeWidth(1);
        }
    }

    public Bounds getBounds(){
        return body.getBoundsInParent();
    }

    private void changeHeight(double height){
        setHeight(height);
        top.setHeight(Math.min(TOP_MAX_HEIGHT, height*TOP_HEIGHT_RATIO));
        body.setHeight(height - top.getHeight());
    }

    private void changeWidth(double width){
        setWidth(width);
        top.setWidth(Math.min(TOP_MAX_WIDTH, width*TOP_WIDTH_RATIO));
        body.setWidth(width);
        title.setWrappingWidth(width - 7);

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
        } else if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
            title.setText((String) evt.getNewValue());

        }
    }
}