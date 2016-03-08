package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.Serializable;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge, Serializable {
    private Node startNode;
    private Node endNode;
    private DoubleProperty zoom = new SimpleDoubleProperty(1);

    public AbstractEdge(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
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

    /**
     * No-arg constructor for JavaBean convention
     */
    public AbstractEdge(){
    }
}
