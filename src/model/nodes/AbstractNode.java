package model.nodes;

import javafx.geometry.Rectangle2D;

import util.Constants;
import util.GlobalVariables;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Node to hide some basic functionality for Nodes.
 */
public abstract class AbstractNode implements Node, Serializable
{
	private static Logger logger = LoggerFactory.getLogger(AbstractNode.class);

    protected static final double CLASS_MIN_WIDTH = 120;
    protected static final double CLASS_MIN_HEIGHT = 100;
    protected static final double LIFELINE_MIN_WIDTH = 120;
    protected static final double LIFELINE_MIN_HEIGHT = 40;
    protected static final double PACKAGE_MIN_WIDTH = 240;
    protected static final double PACKAGE_MIN_HEIGHT = 200;

    private static final long serialVersionUID = 1L;
    protected static int objectCount = 0; //Used to ID instance
    private String id;

    //Listened to by the view, is always fired.
    protected transient PropertyChangeSupport changes = new PropertyChangeSupport(this);
    //Listened to by the server/client, only fired when the change comes from local interaction.
    protected transient PropertyChangeSupport remoteChanges = new PropertyChangeSupport(this);

    protected String aTitle;
    protected double x, y, width, height, translateX, translateY, scaleX, scaleY;
    protected boolean aIsChild;

    public AbstractNode(double x, double y, double width, double height){
    	logger.debug("AbstractNode()");
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        translateX = x;
        translateY = y;
        scaleX = 1.0d;
        scaleY = 1.0d;

        id = UUID.randomUUID().toString();
    }

    public void setIsChild(boolean pIsChild){
    	logger.debug("setIsChild()");
        aIsChild = pIsChild;
        changes.firePropertyChange(Constants.changeNodeIsChild, null, aIsChild);
    }

    public boolean isChild(){
    	logger.debug("isChild()");
        return aIsChild;
    }

    public void setX(double x){
    	logger.debug("setX()");
        this.x = x;
        changes.firePropertyChange(Constants.changeNodeX, null, this.x);
        remoteChanges.firePropertyChange(Constants.changeNodeX, null, this.x);
    }

    public void setY(double y){
    	logger.debug("setY()");
        this.y = y;
        changes.firePropertyChange(Constants.changeNodeY, null, this.y);
        remoteChanges.firePropertyChange(Constants.changeNodeY, null, this.y);
    }

    /**
     * Sets the height of the node. If less than MIN_HEIGHT, height is set to MIN_HEIGHT.
     * @param height
     */
    public void setHeight(double height){
    	logger.debug("setHeight()");
        this.height = height;
        changes.firePropertyChange(Constants.changeNodeHeight, null, this.height);
        remoteChanges.firePropertyChange(Constants.changeNodeHeight, null, this.height);
    }

    /**
     * Sets the width of the node. If less than MIN_WIDTH, width is set to MIN_WIDTH.
     * @param width
     */
    public void setWidth(double width){
    	logger.debug("setWidth()");
        this.width = width;
        changes.firePropertyChange(Constants.changeNodeWidth, null, this.width);
        remoteChanges.firePropertyChange(Constants.changeNodeWidth, null, this.width);
    }

    public void setTitleOnly(String pTitle) {
    	logger.debug("setTitleOnly()");
        this.aTitle = pTitle;
    }    
    
    public void setTitle(String pTitle) {
    	logger.debug("setTitle()");
    	if (aTitle == null) {
           	aTitle = pTitle;
    	}
        changes.firePropertyChange(Constants.changeNodeTitle, null, pTitle);
        if (GlobalVariables.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
            remoteChanges.firePropertyChange(Constants.changeNodeTitle, null, pTitle);
        }
        // TODO: Else only for test, remove when sync button implemented
        else {
            remoteChanges.firePropertyChange(Constants.changeNodeTitle, null, pTitle);
        }
    }

    @Override
    public void setTranslateX(double x) {
    	logger.debug("setTranslateX()");
        translateX = x;
        changes.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
        remoteChanges.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
    }

    @Override
    public void setTranslateY(double y) {
    	logger.debug("setTranslateY()");
        translateY = y;
        changes.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
        remoteChanges.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
    }

    @Override
    public void setScaleX(double x) {
    	logger.debug("setScaleX()");
        scaleX = x;
        changes.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
        remoteChanges.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
    }

    @Override
    public void setScaleY(double y) {
    	logger.debug("setScaleY()");
        scaleY = y;
        changes.firePropertyChange(Constants.changeNodeScaleY, null, scaleY);
        remoteChanges.firePropertyChange(Constants.changeNodeScaleY, null, scaleY);
    }

    public void remoteSetX(double x){
    	logger.debug("remoteSetX()");
        this.x = x;
        changes.firePropertyChange(Constants.changeNodeX, null, this.x);
    }

    public void remoteSetY(double y){
    	logger.debug("remoteSetY()");
        this.y = y;
        changes.firePropertyChange(Constants.changeNodeY, null, this.y);
    }

    public void remoteSetHeight(double height){
    	logger.debug("remoteSetHeight()");
        changes.firePropertyChange(Constants.changeNodeHeight, null, this.height);
    }
    public void remoteSetWidth(double width){
    	logger.debug("remoteSetWidth()");
        changes.firePropertyChange(Constants.changeNodeWidth, null, this.width);
    }

    public void remoteSetTitle(String[] dataArray) {
        logger.debug("remoteSetTitle()");
        logger.info(GlobalVariables.getUserName() + " reveived from " + dataArray[3] + ":\n" +
    			"title '" + dataArray[2] + "'\n");
        if (GlobalVariables.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
           	aTitle = dataArray[2];
        }
        changes.firePropertyChange(Constants.changeNodeTitle, null, dataArray);
    }

    public void remoteSetTranslateX(double x) {
    	logger.debug("remoteSetTranslateX()");
        translateX = x;
        changes.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
    }

    public void remoteSetTranslateY(double y) {
    	logger.debug("remoteSetTranslateY()");
        translateY = y;
        changes.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
    }

    public void remoteSetScaleX(double x) {
    	logger.debug("remoteSetScaleX()");
        scaleX = x;
        changes.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
    }

    public void remoteSetScaleY(double y) {
    	logger.debug("remoteSetScaleY()");
        scaleY = y;
        changes.firePropertyChange(Constants.changeNodeScaleY, null, scaleY);
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getWidth(){
        return width;
    }

    public double getHeight(){
        return height;
    }

    @Override
    public double getTranslateX() {
        return translateX;
    }

    @Override
    public double getTranslateY() {
        return translateY;
    }

    @Override
    public double getScaleX() {
        return scaleX;
    }

    @Override
    public double getScaleY() {
        return scaleY;
    }

    public String getTitle() {
        return aTitle;
    }

    @Override
    public Rectangle2D getBounds() {
    	logger.debug("Rectangle2D()");
        return new Rectangle2D(x, y, width, height);
    }


    public abstract AbstractNode copy();

    @Override
    public String toString() {
    	logger.debug("toString()");
        return super.toString() + " x=" + getX() + " y=" + getY() + " height=" + getHeight() + " width=" + getWidth();
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public AbstractNode(){
    	logger.debug("AbstractNode()");
    }

    public String getId(){
        return "NODE_" + id;
    }
    
    public void setId(String id) {
    	logger.debug("setId()");
		this.id = id;
	}

	public static void incrementObjectCount(){
    	logger.debug("incrementObjectCount()");
        objectCount++;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    	logger.debug("addPropertyChangeListener()");
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    	logger.debug("removePropertyChangeListener()");
        changes.removePropertyChangeListener(l);
    }

    public void addRemotePropertyChangeListener(PropertyChangeListener l) {
    	logger.debug("addRemotePropertyChangeListener()");
        remoteChanges.addPropertyChangeListener(l);
    }

    public void removeRemotePropertyChangeListener(PropertyChangeListener l){
    	logger.debug("removeRemotePropertyChangeListener()");
        remoteChanges.removePropertyChangeListener(l);
    }
}
