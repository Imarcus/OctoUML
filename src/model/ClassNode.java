package model;

import util.Constants;

import java.io.Serializable;
/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class ClassNode extends AbstractNode implements Serializable
{

    private String attributes;
    private String operations;

    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    }


    public void setAttributes(String pAttributes){
        attributes = pAttributes;
        changes.firePropertyChange(Constants.changeClassNodeAttributes, null, attributes);
    }

    public void setOperations(String pOperations){
        operations = pOperations;
        changes.firePropertyChange(Constants.changeClassNodeOperations, null, operations);
    }

    public String getAttributes(){
        return attributes;
    }

    public String getOperations(){
        return operations;
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
        if(this.attributes != null){
            newCopy.setAttributes(this.attributes);
        }
        if(this.operations != null){
            newCopy.setOperations(operations);
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
