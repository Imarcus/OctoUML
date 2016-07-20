package model;

import javafx.beans.property.*;
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
    private static final long serialVersionUID = 1L;
    private static int objectCount = 0; //Used to ID instance
    private int id = 0;

    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private final double MIN_WIDTH = 80;
    private final double MIN_HEIGHT = 70;
    private String aTitle;
    private double x;
    private double y;
    private double width;
    private double height;
    private double translateX;
    private double translateY;
    private double scaleX;
    private double scaleY;
    private boolean aIsChild;

    public AbstractNode(double x, double y, double width, double height){
        this.x = x;
        this.y = y;

        //Don't accept nodes with size less than MIN_WIDTH * MIN_HEIGHT.
        this.width = width < MIN_WIDTH ? MIN_WIDTH : width;
        this.height = height < MIN_HEIGHT ? MIN_HEIGHT : height;

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
    }

    public void setY(double y){
        this.y = y;
        changes.firePropertyChange(Constants.changeNodeY, null, this.y);
    }

    /**
     * Sets the height of the node. If less than MIN_HEIGHT, height is set to MIN_HEIGHT.
     * @param height
     */
    public void setHeight(double height){
        this.height = height < MIN_HEIGHT ? MIN_HEIGHT : height;
        changes.firePropertyChange(Constants.changeNodeHeight, null, this.height);
    }

    /**
     * Sets the width of the node. If less than MIN_WIDTH, width is set to MIN_WIDTH.
     * @param width
     */
    public void setWidth(double width){
        this.width = width < MIN_WIDTH ? MIN_WIDTH : width;
        changes.firePropertyChange(Constants.changeNodeWidth, null, this.width);
    }

    public void setTitle(String pTitle) {
        this.aTitle = pTitle;
        changes.firePropertyChange(Constants.changeNodeTitle, null, aTitle);
    }

    @Override
    public void setTranslateX(double x) {
        translateX = x;
        changes.firePropertyChange(Constants.changeNodeTranslateX, null, translateX);
    }

    @Override
    public void setTranslateY(double y) {
        translateY = y;
        changes.firePropertyChange(Constants.changeNodeTranslateY, null, translateY);
    }

    @Override
    public void setScaleX(double x) {
        scaleX = x;
        changes.firePropertyChange(Constants.changeNodeScaleX, null, scaleX);
    }

    @Override
    public void setScaleY(double y) {
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

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
