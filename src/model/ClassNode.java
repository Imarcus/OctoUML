package model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class ClassNode extends AbstractNode
{
    private SimpleStringProperty attributes = new SimpleStringProperty();
    private SimpleStringProperty operations = new SimpleStringProperty();


    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    }

    public SimpleStringProperty attributesProperty(){
        return attributes;
    }

    public SimpleStringProperty operationsProperty(){
        return operations;
    }

    public void setAttributes(String pAttributes){

        attributes.setValue(pAttributes);
    }

    public void setOperations(String pOperations){
        operations.setValue(pOperations);
    }

    public String getAttributes(){
        return attributes.getValue();
    }

    public String getOperations(){
        return operations.getValue();
    }

    @Override
    public ClassNode copy(){
        ClassNode newCopy = new ClassNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        newCopy.setTranslateX(this.getTranslateX());
        newCopy.setTranslateY(this.getTranslateY());
        newCopy.setScaleX(this.getScaleX());
        newCopy.setScaleY(this.getScaleY());

        if(this.getTitle() != null){
            newCopy.setTitle(this.getTitle());

        }
        if(this.attributesProperty().getValue() != null){
            newCopy.setAttributes(this.attributesProperty().getValue());
        }
        if(this.operationsProperty().getValue() != null){
            newCopy.setOperations(operationsProperty().getValue());
        }
        newCopy.setTranslateX(this.getTranslateX());
        newCopy.setTranslateY(this.getTranslateY());
        return newCopy;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public ClassNode(){
    }
}
