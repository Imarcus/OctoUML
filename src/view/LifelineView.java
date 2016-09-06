package view;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Lifeline;
import util.Constants;

import java.beans.PropertyChangeEvent;

/**
 * Visual representation of ClassNode class.
 */
public class LifelineView extends AbstractNodeView implements NodeView {

    private Label title;
    private Rectangle rectangle;
    private StackPane container;
    private Line shortHandleLine;
    private Line longHandleLine;
    private Line lifeline;

    private final int STROKE_WIDTH = 1;
    private final double LIFELINE_DEFAULT_LENGTH = 500;

    public LifelineView(Lifeline node) {
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

        container.getChildren().addAll(rectangle, title);

    }

    private void createLifeline(){
        lifeline = new Line();//(xPos, yPos, xPos, yPos + LIFELINE_DEFAULT_LENGTH );
        lifeline.startXProperty().bind(rectangle.widthProperty().subtract(rectangle.widthProperty().divide(2)));
        lifeline.startYProperty().bind(rectangle.heightProperty().add(1));
        lifeline.endXProperty().bind(rectangle.widthProperty().subtract(rectangle.widthProperty().divide(2)));
        lifeline.endYProperty().bind(rectangle.heightProperty().add(LIFELINE_DEFAULT_LENGTH));
        lifeline.getStrokeDashArray().addAll(20d, 10d);
        this.getChildren().add(lifeline);
    }

    private void createRectangles(){
        Lifeline node = (Lifeline) getRefNode();
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

    private void initTitle(){
        Lifeline node = (Lifeline) getRefNode();

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
        } else {
            rectangle.setStrokeWidth(1);
            setStroke(Color.BLACK);
        }
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
        }
    }
}