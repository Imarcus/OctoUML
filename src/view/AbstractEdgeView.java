package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import model.AbstractEdge;

/**
 * Created by chris on 2016-02-18.
 */
public abstract class AbstractEdgeView extends Group implements EdgeView{
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private boolean selected = false;
    public final double STROKE_WIDTH = 2;
    public enum Position{
        ABOVE, BELOW, RIGHT, LEFT, NONE
    }

    private Position position = Position.NONE;


    public AbstractEdge getRefEdge() {
        return refEdge;
    }

    private AbstractNodeView endNode;
    private Line line;

    public AbstractEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super();
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setVisible(true);
        line = new Line();
        this.getChildren().add(line);
        setChangeListeners();
        setPosition();
        line.setStrokeWidth(STROKE_WIDTH);
    }

    public void setStrokeWidth(double width) {
        line.setStrokeWidth(width);
    }

    public void setStroke(Paint value){
        line.setStroke(value);
    }

    public double getStartX() {
        return line.getStartX();
    }

    public double getStartY(){
        return line.getStartY();
    }

    public double getEndX(){
        return line.getEndX();
    }

    public double getEndY(){
        return line.getEndY();
    }

    public Line getLine() {
        return line;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
        if (selected){
            line.setStroke(Color.MEDIUMVIOLETRED);
        } else {
            line.setStroke(Color.BLACK);
        }
    }

    public Position getPosition() {
        return position;
    }

    private void setPosition() {
        //IF end node is to the right of startNode:
        if (startNode.getX() + startNode.getWidth() <= endNode.getX()) {
            line.setStartX(startNode.getX() + startNode.getWidth());
            line.setStartY(startNode.getY() + (startNode.getHeight() / 2));
            line.setEndX(endNode.getX());
            line.setEndY(endNode.getY() + (endNode.getHeight() / 2));
            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startNode.getX() > endNode.getX() + endNode.getWidth()) {
            line.setStartX(startNode.getX());
            line.setStartY(startNode.getY() + (startNode.getHeight() / 2));
            line.setEndX(endNode.getX() + endNode.getWidth());
            line.setEndY(endNode.getY() + (endNode.getHeight() / 2));
            position = Position.LEFT;
        }
        // If end node is below startNode:
        else if (startNode.getY() + startNode.getHeight() < endNode.getY()){
            line.setStartX(startNode.getX() + (startNode.getWidth() /2));
            line.setStartY(startNode.getY() + startNode.getHeight());
            line.setEndX(endNode.getX() + (endNode.getWidth()/2));
            line.setEndY(endNode.getY());
            position = Position.BELOW;
        }
        //If end node is above startNode:
        else if (startNode.getY() >= endNode.getY() + endNode.getHeight()) {
            line.setStartX(startNode.getX() + (startNode.getWidth() / 2));
            line.setStartY(startNode.getY());
            line.setEndX(endNode.getX() + (endNode.getWidth()/2));
            line.setEndY(endNode.getY() + endNode.getHeight());
            position = Position.ABOVE;
        }
        //TODO Handle when the nodes are overlapping.
    }

    private void setChangeListeners() {
        startNode.translateXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //setStartX(newValue.doubleValue());
                startNode.setX(newValue.doubleValue());
                setPosition();
            }
        });
        startNode.translateYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                startNode.setY(newValue.doubleValue());
                setPosition();
            }
        });
        endNode.translateXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                endNode.setX(newValue.doubleValue());
                setPosition();
            }
        });
        endNode.translateYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                endNode.setY(newValue.doubleValue());
                setPosition();
            }
        });
    }
}
