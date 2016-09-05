package model;

import util.Constants;

import java.beans.PropertyChangeSupport;

/**
 * Represents an associate relationship between two UML-classes.
 */
public class MessageEdge extends AbstractEdge {

    private double startX;
    private double startY;

    public MessageEdge(double pStartX, double pStartY, Node endNode) {
        super(null, endNode);
        startX = pStartX;
        startY = pStartY;
        setDirection(Direction.START_TO_END);
    }

    public MessageEdge(Node startNode, Node endNode){
        super(startNode, endNode);
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartX(double pStartX) {
        changes.firePropertyChange(Constants.changeMessageStartX, pStartX, startX);
        remoteChanges.firePropertyChange(Constants.changeMessageStartX, pStartX, startX);
        startX = pStartX;

    }

    public void setStartY(double pStartY) {
        changes.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
        remoteChanges.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
        startY = pStartY;
    }

    public void remoteSetStartX(double pStartX) {
        changes.firePropertyChange(Constants.changeMessageStartX, pStartX, startX);
        startX = pStartX;

    }

    public void remoteSetStartY(double pStartY) {
        changes.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
        startY = pStartY;
    }

    @Override
    public void setTranslateX(double x) {

    }

    @Override
    public void setTranslateY(double y) {

    }

    @Override
    public void setScaleX(double x) {

    }

    @Override
    public void setScaleY(double y) {

    }

    @Override
    public double getTranslateX() {
        return 0;
    }

    @Override
    public double getTranslateY() {
        return 0;
    }

    @Override
    public double getScaleX() {
        return 0;
    }

    @Override
    public double getScaleY() {
        return 0;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public MessageEdge(){
    }
    
    public MessageEdge copy(int pStartX, int pStartY, AbstractNode endNodeCopy){
        return new MessageEdge(pStartX, pStartY, endNodeCopy);
    }

    public MessageEdge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy) {
        return new MessageEdge(startNodeCopy, endNodeCopy);
    }

    public String getType(){
        return "Message";
    }
}
