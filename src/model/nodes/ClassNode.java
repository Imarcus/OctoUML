package model.nodes;

import util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Represents a UML class.
 */
public class ClassNode extends AbstractNode implements Serializable
{
	private static final Logger logger = LoggerFactory.getLogger(ClassNode.class);
    private static final String TYPE = "CLASS";
    private List<Attribute> attributes;
    private List<Operation> operations;

    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    	logger.debug("ClassNode()");

        //Don't accept nodes with size less than minWidth * minHeight.
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
    }

    public void setAttributes(List<Attribute> pAttributes) {
    	logger.debug("setAttributes()");
    	attributes = pAttributes;
    }    
    
    public void setAttribute(Attribute pAttribute){
    	logger.debug("setAttributes()");
    	for(Attribute tf: attributes) {
    		if (tf.getId().equals(pAttribute.getId())) {
    			attributes.remove(tf);
    			try {
    				attributes.add(pAttribute.getIndex(), pAttribute);    			
    			} catch(Exception e) {
    				attributes.add(pAttribute);
    			}
    			pAttribute.setIndex(attributes.indexOf(pAttribute));
    		}
    	}
        changes.firePropertyChange(Constants.changeClassNodeAttribute, null, pAttribute);
        remoteChanges.firePropertyChange(Constants.changeClassNodeAttribute, null, pAttribute);
    }

    public void setOperations(List<Operation> pOperation) {
    	logger.debug("setOperations()");
    	operations = pOperation;
    }    

    public void setOperation(Operation pOperation){
    	logger.debug("setOperations()");
    	for(Operation tf: operations) {
    		if (tf.getId().equals(pOperation.getId())) {
    			operations.remove(tf);
    			try {
    				operations.add(pOperation.getIndex(), pOperation);    			
    			} catch(Exception e) {
    				operations.add(pOperation);
    			}
    			pOperation.setIndex(operations.indexOf(pOperation));
    		}
    	}
        changes.firePropertyChange(Constants.changeClassNodeOperation, null, pOperation);
        remoteChanges.firePropertyChange(Constants.changeClassNodeOperation, null, pOperation);
    }

    public void remoteSetAttributes(Object[] dataArray) { 
        logger.debug("remoteSetAttributes()");
        Attribute attribute = (Attribute) dataArray[3]; 
    	for(Attribute tf: attributes) {
    		if (tf.getId().equals(attribute.getId())) {
    			attributes.remove(tf);
    			try {
    				attributes.add(attribute.getIndex(), attribute);    			
    			} catch(Exception e) {
    				attributes.add(attribute);
    			}
    			attribute.setIndex(attributes.indexOf(attribute));
    		}
    	}
        changes.firePropertyChange(Constants.changeClassNodeAttribute, null, dataArray);
    }

    public void remoteSetOperations(Object[] dataArray){
        logger.debug("remoteSetOperations()");
        Operation operation = (Operation) dataArray[3]; 
    	for(Operation tf: operations) {
    		if (tf.getId().equals(operation.getId())) {
    			operations.remove(tf);
    			try {
    				operations.add(operation.getIndex(), operation);    			
    			} catch(Exception e) {
    				operations.add(operation);
    			}
    			operation.setIndex(operations.indexOf(operation));
    		}
    	}
        changes.firePropertyChange(Constants.changeClassNodeOperation, null, dataArray);
    }

    public List<Attribute> getAttributes(){
    	return attributes;
    }

    public List<Operation> getOperations(){
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
            newCopy.setOperations(this.operations);
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
