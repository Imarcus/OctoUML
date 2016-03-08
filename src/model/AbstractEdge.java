package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge {
    private Node startNode;
    private Node endNode;
    private BooleanProperty navigable = new SimpleBooleanProperty();
    private StringProperty startMultiplicity = new SimpleStringProperty();
    private StringProperty endMultiplicity = new SimpleStringProperty();

    public enum Direction {
        NO_DIRECTION, START_TO_END, END_TO_START, BIDIRECTIONAL
    }
    private Direction direction = Direction.NO_DIRECTION;

    public AbstractEdge(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
        navigable.setValue(false);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
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

    public BooleanProperty getNavigableProperty() {
        return navigable;
    }

    public void setNavigable(boolean value) {
        navigable.setValue(value);
    }

    public boolean isNavigable() {
        return navigable.get();
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
}
