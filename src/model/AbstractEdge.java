package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.beans.property.ObjectProperty;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javafx.beans.property.*;
import util.Constants;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge, Serializable {

    private static int objectCount = 0;  //Used to ID instance
    private int id = 0;
    private static final long serialVersionUID = 1L;


    protected transient PropertyChangeSupport changes = new PropertyChangeSupport(this);


    private Node startNode;
    private Node endNode;
    private double zoom;
    private String startMultiplicity, endMultiplicity;

    public enum Direction {
        NO_DIRECTION, START_TO_END, END_TO_START, BIDIRECTIONAL
    }

    private Direction direction;

    public AbstractEdge(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
        direction = Direction.NO_DIRECTION;

        id = ++objectCount;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        changes.firePropertyChange(Constants.changeEdgeDirection, null, direction);
    }

    public Direction getDirection() {
        return direction;
    }


    public void setStartMultiplicity(String pStartMultiplicity) {
        startMultiplicity = pStartMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeStartMultiplicity, null, pStartMultiplicity);
    }

    public void setEndMultiplicity(String pEndMultiplicity) {
        endMultiplicity = pEndMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeEndMultiplicity, null, pEndMultiplicity);
    }

    public String getStartMultiplicity() {
        return startMultiplicity;
    }


    public String getEndMultiplicity() {
        return endMultiplicity;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node node) {
        changes.firePropertyChange(Constants.changeEdgeStartNode, null, node);
        this.startNode = node;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node node) {
        changes.firePropertyChange(Constants.changeEdgeEndNode, null, node);
        this.endNode = node;
    }

    public void setZoom(double scale){
        changes.firePropertyChange(Constants.changeEdgeZoom, null, scale);
        zoom = scale;
    }

    public double getZoom(){
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

    public String getId(){
        return "EDGE_" + id;
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
