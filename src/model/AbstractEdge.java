package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.beans.property.ObjectProperty;
import java.io.Serializable;
import javafx.beans.property.*;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge, Serializable {
    private Node startNode;
    private Node endNode;
    private DoubleProperty zoom = new SimpleDoubleProperty(1);
    private StringProperty startMultiplicity = new SimpleStringProperty();
    private StringProperty endMultiplicity = new SimpleStringProperty();

    public enum Direction {
        NO_DIRECTION, START_TO_END, END_TO_START, BIDIRECTIONAL
    }

    private ObjectProperty<Direction> direction = new SimpleObjectProperty<>();

    public AbstractEdge(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
        direction.setValue(Direction.NO_DIRECTION);
    }

    public void setDirection(Direction direction) {
        this.direction.setValue(direction);
    }

    public Direction getDirection() {
        return direction.getValue();
    }

    public ObjectProperty<Direction> getDirectionProperty() {
        return direction;
    }

    public void setStartMultiplicity(String startMultiplicity) {
        this.startMultiplicity.set(startMultiplicity);
    }

    public void setEndMultiplicity(String endMultiplicity) {
        this.endMultiplicity.set(endMultiplicity);
    }

    public String getStartMultiplicity() {
        return startMultiplicity.get();
    }

    public StringProperty startMultiplicityProperty() {
        return startMultiplicity;
    }

    public String getEndMultiplicity() {
        return endMultiplicity.get();
    }

    public StringProperty endMultiplicityProperty() {
        return endMultiplicity;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node node) {
        this.startNode = node;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node node) {
        this.endNode = node;
    }

    public void setZoom(double scale){
        zoom.setValue(scale);
    }

    public double getZoom(){
        return zoom.getValue();
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    @Override
    public String toString() {
        return super.toString() + this.getClass().toString() + " " + direction + getStartMultiplicity() + getEndMultiplicity();
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public AbstractEdge(){
    }
}
