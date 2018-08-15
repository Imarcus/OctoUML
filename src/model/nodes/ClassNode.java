package model.nodes;

import util.Constants;
import util.GlobalVariables;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Represents a UML class.
 */
public class ClassNode extends AbstractNode implements Serializable
{
	private static final Logger logger = LoggerFactory.getLogger(ClassNode.class);
    private static final String TYPE = "CLASS";
    private String attributes;
    private String operations;

    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    	logger.debug("ClassNode()");

        //Don't accept nodes with size less than minWidth * minHeight.
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
    }

    public void setAttributes(String pAttributes){
    	logger.debug("setAttributes()");
        attributes = pAttributes;
        changes.firePropertyChange(Constants.changeClassNodeAttributes, null, attributes);
        remoteChanges.firePropertyChange(Constants.changeClassNodeAttributes, null, attributes);
    }

    public void setOperations(String pOperations){
    	logger.debug("setOperations()");
        operations = pOperations;
        changes.firePropertyChange(Constants.changeClassNodeOperations, null, operations);
        remoteChanges.firePropertyChange(Constants.changeClassNodeOperations, null, operations);
    }

    public void remoteSetAttributes(String[] pAttributes){
        logger.debug("remoteSetAttributes()");
        logger.info(GlobalVariables.getUserName() + " reveived from " + pAttributes[3] + ":\n" +
    			"attributes:\n"+pAttributes[2]+"\n");
        attributes = pAttributes[2];
        changes.firePropertyChange(Constants.changeClassNodeAttributes, null, attributes);
    }

    public void remoteSetOperations(String[] pOperations){
        logger.debug("remoteSetOperations()");
        logger.info(GlobalVariables.getUserName() + " reveived from " + pOperations[3] + ":\n" +
    			"operations:\n"+pOperations[2]+"\n");
        operations = pOperations[2];
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
    	logger.debug("copy()");
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

    @Override
    public void setHeight(double height) {
    	logger.debug("setHeight()");
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
        super.setHeight(height);
    }

    @Override
    public void setWidth(double width) {
    	logger.debug("setWidth()");
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        super.setWidth(width);
    }

    @Override
    public void remoteSetHeight(double height) {
    	logger.debug("remoteSetHeight()");
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
        super.remoteSetHeight(height);
    }

    @Override
    public void remoteSetWidth(double width) {
    	logger.debug("remoteSetWidth()");
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        super.remoteSetWidth(width);
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public ClassNode(){
    	logger.debug("ClassNode()");
    }

    public String getType(){
    	logger.debug("getType()");
        return TYPE;
    }
}
