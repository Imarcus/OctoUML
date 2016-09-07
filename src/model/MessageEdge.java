package model;

import util.Constants;

import java.beans.PropertyChangeSupport;

/**
 * Represents an associate relationship between two UML-classes.
 */
public class MessageEdge extends AbstractEdge {

    private double startX;
    private double startY;
    private String title;
    public enum MessageType {
        REQUEST, RESPONSE;
    }

    private MessageType messageType = MessageType.REQUEST;

    public MessageEdge(double pStartX, double pStartY, Node endNode) {
        super(null, endNode);
        startX = pStartX;
        startY = pStartY;
        setDirection(Direction.START_TO_END);
    }

    public MessageEdge(double pStartX, double pStartY, Node startNode, Node endNode){
        super(startNode, endNode);
        startX = pStartX;
        startY = pStartY;
        setDirection(Direction.START_TO_END);
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
        Lifeline lowestNode;
        if(startNode != null && startNode.getY() > endNode.getY()){
            lowestNode = (Lifeline)startNode;
        } else {
            lowestNode = (Lifeline)endNode;
        }
        if(pStartY >= lowestNode.getY() + lowestNode.getHeight()){
            changes.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
            remoteChanges.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
            startY = pStartY;
        }
    }

    public void remoteSetStartX(double pStartX) {
        changes.firePropertyChange(Constants.changeMessageStartX, pStartX, startX);
        startX = pStartX;

    }

    public void remoteSetStartY(double pStartY) {
        Lifeline lowestNode;
        if(startNode != null && startNode.getY() > endNode.getY()){
            lowestNode = (Lifeline)startNode;
        } else {
            lowestNode = (Lifeline)endNode;
        }
        if(pStartY >= lowestNode.getY() + lowestNode.getHeight()){
            changes.firePropertyChange(Constants.changeMessageStartY, pStartY, startY);
            startY = pStartY;
        }
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        changes.firePropertyChange(Constants.changeMessageTitle, title, pTitle);
        remoteChanges.firePropertyChange(Constants.changeMessageTitle, title, pTitle);
        title = pTitle;
    }

    public void remoteSetTitle(String pTitle){
        changes.firePropertyChange(Constants.changeMessageTitle, title, pTitle);
        title = pTitle;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public MessageEdge(){
    }
    
    public MessageEdge copy(int pStartX, int pStartY, AbstractNode endNodeCopy){
        return new MessageEdge(pStartX, pStartY, endNodeCopy);
    }

    //TODO needs startX and Y but needs to implement this
    public MessageEdge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy) {
        return null;
    }

    public String getType(){
        return "Message";
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType pMessageType) {
        changes.firePropertyChange(Constants.changeMessageType, messageType, pMessageType);
        remoteChanges.firePropertyChange(Constants.changeMessageType, messageType, pMessageType);
        messageType = pMessageType;
    }

    public void remoteSetMessagesType(MessageType pMessageType){
        changes.firePropertyChange(Constants.changeMessageType, messageType, pMessageType);
        remoteChanges.firePropertyChange(Constants.changeMessageType, messageType, pMessageType);
        messageType = pMessageType;
    }
}
