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
        this.setStrokeWidth(5);
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
        double startNodeWidth = startNode.getWidth() * startNode.getScaleX();
        double startNodeHeight = startNode.getHeight() * startNode.getScaleY();
        double endNodeWidth = endNode.getWidth() * endNode.getScaleX();
        double endNodeHeight = endNode.getHeight() * endNode.getScaleY();

        //IF end node is to the right of startNode:
        if (startNode.getTranslateX() + startNodeWidth <= endNode.getTranslateX()) {
            this.setStartX(startNode.getTranslateX() + startNodeWidth);
            this.setStartY(startNode.getTranslateY() + (startNodeHeight / 2));
            this.setEndX(endNode.getTranslateX());
            this.setEndY(endNode.getTranslateY() + (endNodeHeight / 2));
        }
        //If end node is to the left of startNode:
        else if (startNode.getTranslateX() > endNode.getTranslateX() + endNodeWidth) {
            this.setStartX(startNode.getTranslateX());
            this.setStartY(startNode.getTranslateY() + (startNodeHeight / 2));
            this.setEndX(endNode.getTranslateX() + endNodeWidth);
            this.setEndY(endNode.getTranslateY() + (endNodeHeight / 2));
        }
        // If end node is below startNode:
        else if (startNode.getTranslateY() + startNodeHeight < endNode.getTranslateY()){
            this.setStartX(startNode.getTranslateX() + (startNodeWidth /2));
            this.setStartY(startNode.getTranslateY() + startNodeHeight);
            this.setEndX(endNode.getTranslateX() + (endNodeWidth/2));
            this.setEndY(endNode.getTranslateY());
        }
        //If end node is above startNode:
        else if (startNode.getTranslateY() >= endNode.getTranslateY() + endNodeHeight) {
            this.setStartX(startNode.getTranslateX() + (startNodeWidth / 2));
            this.setStartY(startNode.getTranslateY());
            this.setEndX(endNode.getTranslateX() + (endNodeWidth/2));
            this.setEndY(endNode.getTranslateY() + endNodeHeight);
        }
        //TODO Handle when the nodes are overlapping.
    }

    private void setChangeListeners() {
        startNode.translateXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPosition();
            }
        });
        startNode.translateYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPosition();
            }
        });
        endNode.translateXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPosition();
            }
        });
        endNode.translateYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPosition();
            }
        });
        refEdge.zoomProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setStrokeWidth(newValue.doubleValue());
                setPosition();
            }
        });
    }
}
