package model;

import javafx.beans.property.*;
import javafx.geometry.Rectangle2D;

import java.io.Serializable;

/**
 * Abstract Node to hide some basic functionality for Nodes.
 */
public abstract class AbstractNode implements Node, Serializable
{
    private final double MIN_WIDTH = 80;
    private final double MIN_HEIGHT = 70;
    private StringProperty aTitle = new SimpleStringProperty();
    private DoubleProperty x = new SimpleDoubleProperty();
    private DoubleProperty y = new SimpleDoubleProperty();
    private DoubleProperty width = new SimpleDoubleProperty();
    private DoubleProperty height = new SimpleDoubleProperty();
    private DoubleProperty translateX = new SimpleDoubleProperty();
    private DoubleProperty translateY = new SimpleDoubleProperty();
    private DoubleProperty scaleX = new SimpleDoubleProperty();
    private DoubleProperty scaleY = new SimpleDoubleProperty();
    private boolean aIsChild;

    public AbstractNode(double x, double y, double width, double height){
        this.x.setValue(x);
        this.y.setValue(y);

        //Don't accept nodes with size less than MIN_WIDTH * MIN_HEIGHT.
        this.width.setValue(width < MIN_WIDTH ? MIN_WIDTH : width);
        this.height.setValue(height < MIN_HEIGHT ? MIN_HEIGHT : height);

        translateX.setValue(x);
        translateY.setValue(y);
        scaleX.setValue(1.0d);
        scaleY.setValue(1.0d);
    }

    public void setIsChild(boolean pIsChild){
        aIsChild = pIsChild;
    }

    public boolean isChild(){
        return aIsChild;
    }

    public void setX(double x){
        this.x.setValue(x);
    }

    public void setY(double y){
        this.y.setValue(y);
    }

    /**
     * Sets the height of the node. If less than MIN_HEIGHT, height is set to MIN_HEIGHT.
     * @param height
     */
    public void setHeight(double height){
        this.height.setValue(height < MIN_HEIGHT ? MIN_HEIGHT : height);
    }

    /**
     * Sets the width of the node. If less than MIN_WIDTH, width is set to MIN_WIDTH.
     * @param width
     */
    public void setWidth(double width){
        this.width.setValue(width < MIN_WIDTH ? MIN_WIDTH : width);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public StringProperty titleProperty() {
        return aTitle;
    }

    public double getTranslateX() {
        return translateX.get();
    }

    public DoubleProperty translateXProperty() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY.get();
    }

    public DoubleProperty translateYProperty() {
        return translateY;
    }

    public double getScaleX() {
        return scaleX.get();
    }

    public DoubleProperty scaleXProperty() {
        return scaleX;
    }

    public double getScaleY() {
        return scaleY.get();
    }

    public DoubleProperty scaleYProperty() {
        return scaleY;
    }


    @Override
    public double getX() {
        return x.getValue();
    }

    @Override
    public double getY() {
        return y.getValue();
    }

    @Override
    public double getWidth() {
        return width.get();
    }

    @Override
    public double getHeight() {
        return height.get();
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D(x.get(), y.get(), width.get(), height.get());
    }

    public String getTitle() {
        return aTitle.get();
    }

    public void setTitle(String aTitle) {
        this.aTitle.setValue(aTitle);
    }

    @Override
    public void setTranslateX(double x) {
        translateX.setValue(x);
    }

    @Override
    public void setTranslateY(double y) {
        translateY.setValue(y);
    }

    @Override
    public void setScaleX(double x) {
        scaleX.setValue(x);
    }

    @Override
    public void setScaleY(double y) {
        scaleY.setValue(y);
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
}
