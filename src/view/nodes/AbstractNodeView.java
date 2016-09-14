package view.nodes;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import model.nodes.AbstractNode;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Visual representation of AbstractNode class.
 */
public abstract class AbstractNodeView extends Group implements NodeView, PropertyChangeListener {

    private static int objectCounter = 0;

    private AbstractNode refNode;

    private double x;
    private double y;
    private double width;
    private double height;

    public AbstractNodeView(AbstractNode node){
        this.setId("VIEWCLASS_" + objectCounter);
        this.refNode = node;

        setX(refNode.getX());
        setY(refNode.getY());
        setHeight(refNode.getHeight());
        setWidth(refNode.getWidth());
        refNode.addPropertyChangeListener(this);
    }

    public AbstractNode getRefNode(){
        return refNode;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean contains(double x, double y) {
        return (x >= this.getTranslateX() && x <= this.getTranslateX() + this.width && y >= this.getTranslateY() && y <= this.getTranslateY() + this.height);
    }

    public abstract Bounds getBounds();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Constants.changeNodeTranslateX)) {
            setTranslateX((double)evt.getNewValue());
        } else if(evt.getPropertyName().equals(Constants.changeNodeTranslateY)) {
            setTranslateY((double)evt.getNewValue());
        } else if(evt.getPropertyName().equals(Constants.changeNodeScaleX)) {
            setScaleX((double)evt.getNewValue());
        } else if(evt.getPropertyName().equals(Constants.changeNodeScaleY)) {
            setScaleY((double)evt.getNewValue());
        }
    }
}


