package model.edges;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import model.nodes.Node;
import util.Constants;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge, Serializable {

    private static int objectCount = 0;  //Used to ID instance
    private int id = 0;
    private static final long serialVersionUID = 1L;

    //Listened to by the view, is always fired.
    protected transient PropertyChangeSupport changes = new PropertyChangeSupport(this);
    //Listened to by the server/client, only fired when the change comes from local interaction.
    protected transient PropertyChangeSupport remoteChanges = new PropertyChangeSupport(this);


    protected Node startNode;
    protected Node endNode;
    protected double zoom;
    protected String startMultiplicity, endMultiplicity;

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

    public void setDirection(Direction pDirection) {
        direction = pDirection;
        changes.firePropertyChange(Constants.changeEdgeDirection, null, direction);
        remoteChanges.firePropertyChange(Constants.changeEdgeDirection, null, direction);
    }

    public void setStartMultiplicity(String pStartMultiplicity) {
        startMultiplicity = pStartMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeStartMultiplicity, null, startMultiplicity);
        remoteChanges.firePropertyChange(Constants.changeEdgeStartMultiplicity, null, startMultiplicity);
    }

    public void setEndMultiplicity(String pEndMultiplicity) {
        endMultiplicity = pEndMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeEndMultiplicity, null, endMultiplicity);
        remoteChanges.firePropertyChange(Constants.changeEdgeEndMultiplicity, null, endMultiplicity);
    }

    public void setStartNode(Node pNode) {
        this.startNode = pNode;
        changes.firePropertyChange(Constants.changeEdgeStartNode, null, startNode);
        remoteChanges.firePropertyChange(Constants.changeEdgeStartNode, null, startNode);
    }

    public void setEndNode(Node pNode) {
        endNode = pNode;
        changes.firePropertyChange(Constants.changeEdgeEndNode, null, endNode);
        remoteChanges.firePropertyChange(Constants.changeEdgeEndNode, null, endNode);
    }

    public void setZoom(double scale){
        zoom = scale;
        changes.firePropertyChange(Constants.changeEdgeZoom, null, zoom);
        remoteChanges.firePropertyChange(Constants.changeEdgeZoom, null, zoom);
    }

    public void remoteSetDirection(Direction pDirection) {
        this.direction = pDirection;
        changes.firePropertyChange(Constants.changeEdgeDirection, null, direction);
    }

    public void remoteSetStartMultiplicity(String pStartMultiplicity) {
        startMultiplicity = pStartMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeStartMultiplicity, null, startMultiplicity);
    }

    public void remoteSetEndMultiplicity(String pEndMultiplicity) {
        endMultiplicity = pEndMultiplicity;
        changes.firePropertyChange(Constants.changeEdgeEndMultiplicity, null, endMultiplicity);
    }

    public void remoteSetStartNode(Node pNode) {
        this.startNode = pNode;
        changes.firePropertyChange(Constants.changeEdgeStartNode, null, startNode);
    }

    public void remoteSetEndNode(Node pNode) {
        this.endNode = pNode;
        changes.firePropertyChange(Constants.changeEdgeEndNode, null, endNode);
    }

    public void remoteSetZoom(double scale){
        zoom = scale;
        changes.firePropertyChange(Constants.changeEdgeZoom, null, zoom);
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

    public Node getEndNode() {
        return endNode;
    }

    public double getZoom(){
        return zoom;
    }

    public Direction getDirection() {
        return direction;
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

    public static void incrementObjectCount(){
        objectCount++;
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
