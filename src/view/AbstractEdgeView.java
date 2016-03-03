package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import model.AbstractEdge;

/**
 * Created by chris on 2016-02-18.
 */
public abstract class AbstractEdgeView extends Line implements EdgeView{
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private boolean selected = false;

    public AbstractEdge getRefEdge() {
        return refEdge;
    }

    private AbstractNodeView endNode;

    public AbstractEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setVisible(true);
        setChangeListeners();
        setPosition();
        this.setStrokeWidth(10);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
        if (selected){
            setStroke(Color.MEDIUMVIOLETRED);
        } else {
            setStroke(Color.BLACK);
        }
    }

    private void setPosition() {
        //IF end node is to the right of startNode:
        if (startNode.getX() + startNode.getWidth() <= endNode.getX()) {
            this.setStartX(startNode.getX() + startNode.getWidth());
            this.setStartY(startNode.getY() + (startNode.getHeight() / 2));
            this.setEndX(endNode.getX());
            this.setEndY(endNode.getY() + (endNode.getHeight() / 2));
        }
        //If end node is to the left of startNode:
        else if (startNode.getX() > endNode.getX() + endNode.getWidth()) {
            this.setStartX(startNode.getX());
            this.setStartY(startNode.getY() + (startNode.getHeight() / 2));
            this.setEndX(endNode.getX() + endNode.getWidth());
            this.setEndY(endNode.getY() + (endNode.getHeight() / 2));
        }
        // If end node is below startNode:
        else if (startNode.getY() + startNode.getHeight() < endNode.getY()){
            this.setStartX(startNode.getX() + (startNode.getWidth() /2));
            this.setStartY(startNode.getY() + startNode.getHeight());
            this.setEndX(endNode.getX() + (endNode.getWidth()/2));
            this.setEndY(endNode.getY());
        }
        //If end node is above startNode:
        else if (startNode.getY() >= endNode.getY() + endNode.getHeight()) {
            this.setStartX(startNode.getX() + (startNode.getWidth() / 2));
            this.setStartY(startNode.getY());
            this.setEndX(endNode.getX() + (endNode.getWidth()/2));
            this.setEndY(endNode.getY() + endNode.getHeight());
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
