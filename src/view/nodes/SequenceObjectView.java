package view.nodes;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import model.nodes.SequenceObject;
import util.Constants;

import java.beans.PropertyChangeEvent;

/**
 * Visual representation of ClassNode class.
 */
public class SequenceObjectView extends AbstractNodeView implements NodeView {

    private Label title;
    private Rectangle rectangle;
    private StackPane container;
    private Line shortHandleLine;
    private Line longHandleLine;
    private Line lifeline;
    private Rectangle rectangleHandle;

    private final int STROKE_WIDTH = 1;


    public SequenceObjectView(SequenceObject node) {
        super(node);

        container = new StackPane();
        rectangle = new Rectangle();
        title = new Label();

        initTitle();
        createRectangles();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        initLooks();

        this.getChildren().add(container);
        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());
        createHandles();
        createLifeline();
        createRectangleHandle();

        container.getChildren().addAll(rectangle, title);
    }

    private void createLifeline(){
        lifeline = new Line();//
        lifeline.startXProperty().bind(rectangle.widthProperty().subtract(rectangle.widthProperty().divide(2)));
        lifeline.startYProperty().bind(rectangle.heightProperty().add(1));
        lifeline.endXProperty().bind(rectangle.widthProperty().subtract(rectangle.widthProperty().divide(2)));
        lifeline.endYProperty().bind(rectangle.heightProperty().add(((SequenceObject)getRefNode()).getLifelineLength()));
        lifeline.getStrokeDashArray().addAll(20d, 10d);
        this.getChildren().add(lifeline);
    }

    private void createRectangles(){
        SequenceObject node = (SequenceObject) getRefNode();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());
    }

    private void changeHeight(double height){
        setHeight(height);
        rectangle.setHeight(height);
    }

    private void changeWidth(double width){
        setWidth(width);
        rectangle.setWidth(width);
        container.setMaxWidth(width);
        container.setPrefWidth(width);

        title.setMaxWidth(width);
        title.setPrefWidth(width);
    }

    private void createHandles(){
        shortHandleLine = new Line();
        longHandleLine = new Line();

        shortHandleLine.startXProperty().bind(rectangle.widthProperty().subtract(7));
        shortHandleLine.startYProperty().bind(rectangle.heightProperty().subtract(3));
        shortHandleLine.endXProperty().bind(rectangle.widthProperty().subtract(3));
        shortHandleLine.endYProperty().bind(rectangle.heightProperty().subtract(7));
        longHandleLine.startXProperty().bind(rectangle.widthProperty().subtract(15));
        longHandleLine.startYProperty().bind(rectangle.heightProperty().subtract(3));
        longHandleLine.endXProperty().bind(rectangle.widthProperty().subtract(3));
        longHandleLine.endYProperty().bind(rectangle.heightProperty().subtract(15));

        this.getChildren().addAll(shortHandleLine, longHandleLine);
    }

    private void createRectangleHandle(){
        rectangleHandle = new Rectangle();
        rectangleHandle.setWidth(10);
        rectangleHandle.setHeight(10);
        rectangleHandle.xProperty().bind(lifeline.endXProperty().subtract(rectangleHandle.widthProperty().divide(2)));
        rectangleHandle.yProperty().bind(lifeline.endYProperty().subtract(rectangleHandle.heightProperty().divide(2)));
        this.getChildren().add(rectangleHandle);
        rectangleHandle.setVisible(false);
    }

    private void initTitle(){
        SequenceObject node = (SequenceObject) getRefNode();

        title = new Label();
        title.setFont(Font.font("Verdana", 12));
        if(node.getTitle() != null) {
            title.setText(node.getTitle());
        }
        title.setAlignment(Pos.CENTER);
    }

    private void initLooks(){
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);
        StackPane.setAlignment(title, Pos.CENTER);
    }

    public void setSelected(boolean selected){
        if(selected){
            rectangle.setStrokeWidth(2);
            setStroke(Constants.selected_color);
            lifeline.setStroke(Constants.selected_color);
            rectangleHandle.setFill(Constants.selected_color);
        } else {
            rectangle.setStrokeWidth(1);
            setStroke(Color.BLACK);
            lifeline.setStroke(Color.BLACK);
            rectangleHandle.setFill(Color.BLACK);
        }
        rectangleHandle.setVisible(selected);
    }

    public void setStrokeWidth(double scale){
        rectangle.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
        rectangle.setFill(p);
    }

    public void setStroke(Paint p) {
        rectangle.setStroke(p);
    }

    public Bounds getBounds(){
        return container.getBoundsInParent();
    }

    public Rectangle getLifelineHandle(){
        return rectangleHandle;
    }

    public boolean isOnLifeline(Point2D point){
        Double lifelineXPosition = lifeline.getStartX() + this.getX();
        if(point.getX() > (lifelineXPosition - 10) && point.getX() < (lifelineXPosition + 10)){
            Double lifelineYStartPosition = lifeline.getStartY() + this.getY();
            Double lifelineYEndPosition = lifeline.getEndY() + this.getY();
            if(point.getY() > lifelineYStartPosition && point.getY() < lifelineYEndPosition){
                return true;
            }
        }
        return false;
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
        } else if(evt.getPropertyName().equals(Constants.changeLifelineLength)){
            lifeline.endYProperty().bind(rectangle.heightProperty().add(((SequenceObject)getRefNode()).getLifelineLength()));
        }
    }
}
