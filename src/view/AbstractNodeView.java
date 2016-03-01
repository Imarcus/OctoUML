package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import model.AbstractNode;

/**
 * Created by marcusisaksson on 2016-02-17.
 */
public abstract class AbstractNodeView extends Group implements NodeView {

    private AbstractNode refNode;

    private double x;
    private double y;
    private double width;
    private double height;

    public AbstractNodeView(AbstractNode node){
        this.refNode = node;

        setX(refNode.getX());
        setY(refNode.getY());
        setHeight(refNode.getHeight());
        setWidth(refNode.getWidth());

        setChangeListeners();
    }

    protected AbstractNode getRefNode(){
        return refNode;
    }

    //TODO Shouldn't be used, as this should only get changes from model.
    public void setX(double x) {
        this.x = x;
    }

    //TODO Shouldn't be used, as this should only get changes from model.
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
        if (x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height) {
            return true;
        }
        return false;
    }

    public abstract Bounds getBounds();

    //TODO Maybe needs some Nullchecks etc?
    private void setChangeListeners() {

        refNode.translateXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setTranslateX(newValue.doubleValue());
            }
        });

        refNode.translateYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setTranslateY(newValue.doubleValue());
            }
        });

        refNode.scaleXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setScaleX(newValue.doubleValue());
            }
        });

        refNode.scaleYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setScaleY(newValue.doubleValue());
            }
        });
    }


}


