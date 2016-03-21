package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
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
    private Text startMultiplicity;
    private Text endMultiplicity;

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
        startMultiplicity = new Text(edge.getStartMultiplicity());
        endMultiplicity = new Text(edge.getEndMultiplicity());
        setChangeListeners();
        setPosition();
        line.setStrokeWidth(STROKE_WIDTH);
    }

    protected void draw() {
        //Draw multiplicity
        Position position = getPosition();
        final double OFFSET = 20;

        switch (position) {
            case RIGHT:
                startMultiplicity.setX(getLine().getStartX() + OFFSET);
                startMultiplicity.setY(getLine().getStartY() + OFFSET);
                endMultiplicity.setX(getLine().getEndX() - OFFSET - endMultiplicity.getText().length() -5);
                endMultiplicity.setY(getLine().getEndY() + OFFSET);
                break;
            case LEFT:
                startMultiplicity.setX(getLine().getStartX() - OFFSET - endMultiplicity.getText().length() -5);
                startMultiplicity.setY(getLine().getStartY() + OFFSET);
                endMultiplicity.setX(getLine().getEndX() + OFFSET);
                endMultiplicity.setY(getLine().getEndY() + OFFSET);
                break;
            case ABOVE:
                startMultiplicity.setX(getLine().getStartX() + OFFSET);
                startMultiplicity.setY(getLine().getStartY() - OFFSET);
                endMultiplicity.setX(getLine().getEndX() + OFFSET);
                endMultiplicity.setY(getLine().getEndY() + OFFSET);
                break;
            case BELOW:
                startMultiplicity.setX(getLine().getStartX() + OFFSET);
                startMultiplicity.setY(getLine().getStartY() + OFFSET);
                endMultiplicity.setX(getLine().getEndX() + OFFSET);
                endMultiplicity.setY(getLine().getEndY() - OFFSET);
                break;
        }
        startMultiplicity.toFront();
        endMultiplicity.toFront();
        //TODO This doesn't seem to work?
        //getChildren().add(startMultiplicity);
        //getChildren().add(endMultiplicity);
    }

    public Text getStartMultiplicity() {
        return startMultiplicity;
    }

    public Text getEndMultiplicity() {
        return endMultiplicity;
    }

    public AbstractEdge getRefEdge() {
        return refEdge;
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
        //If end node is to the right of startNode:
        if (startNode.getTranslateX() + startNode.getWidth() <= endNode.getTranslateX()) {
            line.setStartX(startNode.getTranslateX() + startNode.getWidth());
            line.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
            line.setEndX(endNode.getTranslateX());
            line.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));
            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startNode.getTranslateX() > endNode.getTranslateX() + endNode.getWidth()) {
            line.setStartX(startNode.getTranslateX());
            line.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
            line.setEndX(endNode.getTranslateX() + endNode.getWidth());
            line.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));
            position = Position.LEFT;
        }
        // If end node is below startNode:
        else if (startNode.getTranslateY() + startNode.getHeight() < endNode.getTranslateY()){
            line.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
            line.setStartY(startNode.getTranslateY() + startNode.getHeight());
            line.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            line.setEndY(endNode.getTranslateY());
            position = Position.BELOW;
        }
        //If end node is above startNode:
        else if (startNode.getTranslateY() >= endNode.getTranslateY() + endNode.getHeight()) {
            line.setStartX(startNode.getTranslateX() + (startNode.getWidth() / 2));
            line.setStartY(startNode.getTranslateY());
            line.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            line.setEndY(endNode.getTranslateY() + endNode.getHeight());
            position = Position.ABOVE;
        }
        //TODO Handle when the nodes are overlapping.
    }

    public AbstractNodeView getStartNode() {
        return startNode;
    }

    public void setStartNode(AbstractNodeView startNode) {
        this.startNode = startNode;
    }

    public AbstractNodeView getEndNode() {
        return endNode;
    }

    public void setEndNode(AbstractNodeView endNode) {
        this.endNode = endNode;
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

        refEdge.startMultiplicityProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                startMultiplicity.setText(newValue);
                draw();
            }
        });

        refEdge.endMultiplicityProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                endMultiplicity.setText(newValue);
                draw();
            }
        });
    }
}
