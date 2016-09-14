package model.nodes;

import javafx.geometry.Rectangle2D;
import util.Constants;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Abstract Node to hide some basic functionality for Nodes.
 */
public abstract class AbstractNode implements Node, Serializable
{
    protected static final double CLASS_MIN_WIDTH = 120;
    protected static final double CLASS_MIN_HEIGHT = 100;
    protected static final double LIFELINE_MIN_WIDTH = 120;
    protected static final double LIFELINE_MIN_HEIGHT = 40;
    protected static final double PACKAGE_MIN_WIDTH = 240;
    protected static final double PACKAGE_MIN_HEIGHT = 200;

    private static final long serialVersionUID = 1L;
    protected static int objectCount = 0; //Used to ID instance
    private int id;

    //Listened to by the view, is always fired.
    protected transient PropertyChangeSupport changes = new PropertyChangeSupport(this);
    //Listened to by the server/client, only fired when the change comes from local interaction.
    protected transient PropertyChangeSupport remoteChanges = new PropertyChangeSupport(this);

    protected String aTitle;
    protected double x, y, width, height, translateX, translateY, scaleX, scaleY;
    protected boolean aIsChild;

    public AbstractNode(double x, double y, double width, double height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        translateX = x;
        translateY = y;
        scaleX = 1.0d;
        scaleY = 1.0d;

        id = ++objectCount;
    }

    public void setIsChild(boolean pIsChild){
        aIsChild = pIsChild;
        changes.firePropertyChange(Constants.changeNodeIsChild, null, aIsChild);
    }

    public boolean isChild(){
        return aIsChild;
    }

    public void setX(double x){
        this.x = x;
        changes.firePropertyChange(Constants.changeNodeX, null, this.x);
        remoteChanges.firePropertyChange(Constants.changeNodeX, null, this.x);
    }

    public void setY(double y){
        this.y = y;
        changes.firePropertyChange(Constants.changeNodeY, null, this.y);
        remoteChanges.firePropertyChange(Constants.changeNodeY, null, this.y);
    }

    /**
     * Sets the height of the node. If less than MIN_HEIGHT, height is set to MIN_HEIGHT.
     * @param height
     */
    public void setHeight(double height){
        this.height = height;
        changes.firePropertyChange(Constants.changeNodeHeight, null, this.height);
        remoteChanges.firePropertyChange(Constants.changeNodeHeight, null, this.height);
    }

    /**
     * Sets the width of the node. If less than MIN_WIDTH, width is set to MIN_WIDTH.
     * @param width
     */
    public void setWidth(double width){
        this.width = width;
        changes.firePropertyChange(Constants.changeNodeWidth, null, this.width);
        remoteChanges.firePropertyChange(Constants.changeNodeWidth, null, this.width);
    }

    public void setTitle(String pTitle) {
        this.aTitle = pTitle;
        changes.firePropertyChange(Constants.changeNodeTitle, null, aTitle);
        remoteChanges.firePropertyChange(Constants.changeNodeTitle, null, aTitle);
    }

    @Override
    public void setTranslateX(double x) {
        translateX = x;
        changes.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
        remoteChanges.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
    }

    @Override
    public void setTranslateY(double y) {
        translateY = y;
        changes.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
        remoteChanges.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
    }

    @Override
    public void setScaleX(double x) {
        scaleX = x;
        changes.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
        remoteChanges.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
    }

    @Override
    public void setScaleY(double y) {
        scaleY = y;
        changes.firePropertyChange(Constants.changeNodeScaleY, null, scaleY);
        remoteChanges.firePropertyChange(Constants.changeNodeScaleY, null, scaleY);
    }

    public void remoteSetX(double x){
        this.x = x;
        changes.firePropertyChange(Constants.changeNodeX, null, this.x);
    }

    public void remoteSetY(double y){
        this.y = y;
        changes.firePropertyChange(Constants.changeNodeY, null, this.y);
    }

    public void remoteSetHeight(double height){
        changes.firePropertyChange(Constants.changeNodeHeight, null, this.height);
    }
    public void remoteSetWidth(double width){
        changes.firePropertyChange(Constants.changeNodeWidth, null, this.width);
    }

    public void remoteSetTitle(String pTitle) {
        this.aTitle = pTitle;
        changes.firePropertyChange(Constants.changeNodeTitle, null, aTitle);
    }

    public void remoteSetTranslateX(double x) {
        translateX = x;
        changes.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
    }

    public void remoteSetTranslateY(double y) {
        translateY = y;
        changes.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
    }

    public void remoteSetScaleX(double x) {
        scaleX = x;
        changes.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
    }

    public void remoteSetScaleY(double y) {
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
        return new Rectangle2D(x, y, width, height);
    }


    public abstract AbstractNode copy();

    @Override
    public String toString() {
        return super.toString() + " x=" + getX() + " y=" + getY() + " height=" + getHeight() + " width=" + getWidth();
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public AbstractNode(){

    }

    public String getId(){
        return "NODE_" + id;
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

    public void addRemotePropertyChangeListener(PropertyChangeListener l) {
        remoteChanges.addPropertyChangeListener(l);
    }

    public void removeRemotePropertyChangeListener(PropertyChangeListener l){
        remoteChanges.removePropertyChangeListener(l);
    }
}
