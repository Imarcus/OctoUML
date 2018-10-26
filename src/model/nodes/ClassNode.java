package model.nodes;

import util.Constants;
import util.GlobalVariables;
import weka.core.AdditionalMeasureProducer;

import java.io.Serializable;
import java.util.ArrayList;
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
    private List<String> attributes = new ArrayList<>();
    private List<String> operations = new ArrayList<>();

    public ClassNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    	logger.debug("ClassNode()");

        //Don't accept nodes with size less than minWidth * minHeight.
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
    }
    
    private int indexOfAttribute(Attribute newValue) {
    	int cont = 0;
    	for(String oldValueStr: attributes) {
    		Attribute oldValue = new Attribute("");
    		oldValue.toString(oldValueStr);
    		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
    			return cont;
    		}
    		cont++;
    	}
    	return -1;
    }

    private int indexOfOperation(Operation newValue) {
    	int cont = 0;
    	for(String oldValueStr: operations) {
    		Operation oldValue = new Operation("");
    		oldValue.toString(oldValueStr);
    		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
    			return cont;
    		}
    		cont++;
    	}
    	return -1;
    }

    public void setAttributesStr(List<String> pAttributes) {
    	logger.debug("setAttributes(List<String>)");
    	attributes = pAttributes;
    }      
    
    public void setAttributes(List<Attribute> pAttributes) {
    	logger.debug("setAttributes(List<Attribute>)");
		attributes.clear();
    	for(Attribute attribute: pAttributes) {
    		attributes.add(attribute.toString());
    	}
    }
    
    private void removeAttribute(Attribute newValue) {
    	if (newValue != null) {
        	for(String oldValueStr: attributes) {
        		Attribute oldValue = new Attribute("");
        		oldValue.toString(oldValueStr);
        		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
        			attributes.remove(oldValueStr);
            		break;
        		}
        	}    	
    	}
    }

    private void removeOperation(Operation newValue) {
    	for(String oldValueStr: operations) {
    		Operation oldValue = new Operation("");
    		oldValue.toString(oldValueStr);
    		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
    			operations.remove(oldValueStr);
        		break;
    		}
    	}    	
    }    

    public void setAttributeOnly(int index, Attribute newValue){
    	logger.debug("setAttributeOnly()");
    	if (index == -1) {
    		removeAttribute(newValue);
    	} else {
    		boolean found = false;
        	for(String oldValueStr: attributes) {
        		Attribute oldValue = new Attribute("");
        		oldValue.toString(oldValueStr);
        		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
        			found = true;
        			attributes.remove(oldValueStr);
        			try {
        				attributes.add(index, newValue.toString());    			
        			} catch(Exception e) {
        				attributes.add(newValue.toString());
        			}
            		break;
        		}
        	}
        	if (!found) {
    			try {
    				attributes.add(index, newValue.toString());    			
    			} catch(Exception e) {
    				attributes.add(newValue.toString());
    			}
        	}
    	}
    }
    
    
    public void setAttribute(int index, Attribute newValue, boolean transmit, String collaborationType){
    	logger.debug("setAttribute()");
       	setAttributeOnly(index, newValue);
       	String newValueStr;
    	if (index == -1) {
        	newValueStr = index + "|" + newValue;
    	} else {
        	newValueStr = indexOfAttribute(newValue) + "|" + newValue;
    	}

    	changes.firePropertyChange(Constants.changeClassNodeAttribute, null, newValueStr);

        if (collaborationType.equals(Constants.collaborationTypeSynchronous)
        		|| transmit ) {
            remoteChanges.firePropertyChange(Constants.changeClassNodeAttribute, null, newValueStr);
        }
    }

    public void setOperationsStr(List<String> pOperation) {
    	logger.debug("setOperations(List<String>)");
		operations = pOperation;
    }     

    public void setOperations(List<Operation> pOperation) {
    	logger.debug("setOperations(List<Operation>)");
		operations.clear();
    	for(Operation operation: pOperation) {
    		operations.add(operation.toString());
    	}
    }
    
    public void setOperationOnly(int index, Operation newValue){
    	logger.debug("setOperationOnly()");
    	if (index == -1) {
    		removeOperation(newValue);
    	} else {
    		boolean found = false;
        	for(String oldValueStr: operations) {
        		Operation oldValue = new Operation("");
        		oldValue.toString(oldValueStr);
        		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
        			found = true;
        			operations.remove(oldValueStr);
        			try {
        				operations.add(index, newValue.toString());    			
        			} catch(Exception e) {
        				operations.add(newValue.toString());
        			}
            		break;
        		}
        	}
        	if (!found) {
    			try {
    				operations.add(index, newValue.toString());    			
    			} catch(Exception e) {
    				operations.add(newValue.toString());
    			}
        	}
    	}
    }
    
    public void setOperation(int index, Operation newValue, boolean transmit, String collaborationType){
    	logger.debug("setOperation()");
    	if (operations == null) {
    		operations = new ArrayList<>();
    	}
       	setOperationOnly(index, newValue);
       	String newValueStr;
    	if (index == -1) {
        	newValueStr = index + "|" + newValue;
    	} else {
        	newValueStr = indexOfOperation(newValue) + "|" + newValue;
    	}

    	changes.firePropertyChange(Constants.changeClassNodeOperation, null, newValueStr);
    	
        if (collaborationType.equals(Constants.collaborationTypeSynchronous)
        		|| transmit) {
            remoteChanges.firePropertyChange(Constants.changeClassNodeOperation, null, newValueStr);
        }
    }

    public void remoteSetAttribute(String[] dataArray, String collaborationType) { 
        logger.debug("remoteSetAttributes()");
        if (collaborationType.equals(Constants.collaborationTypeSynchronous)) {
            String newValueStr = (String) dataArray[2];
            int index = Integer.parseInt(newValueStr.substring(0, newValueStr.indexOf("|")));
            Attribute newValue = new Attribute("");
            newValue.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
        	for(String oldValueStr: attributes) {
        		Attribute oldValue = new Attribute("");
        		oldValue.toString(oldValueStr);
        		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
        			attributes.remove(oldValueStr);
        			try {
        				attributes.add(index, newValue.toString());    			
        			} catch(Exception e) {
        				attributes.add(newValue.toString());
        			}
            		break;
        		}
        	}
        }        
        changes.firePropertyChange(Constants.changeClassNodeAttribute, null, dataArray);
    }

    public void remoteSetOperation(Object[] dataArray, String collaborationType){
        logger.debug("remoteSetOperations()");
        if (collaborationType.equals(Constants.collaborationTypeSynchronous)) {
            String newValueStr = (String) dataArray[2];
            int index = Integer.parseInt(newValueStr.substring(0, newValueStr.indexOf("|")));
            Operation newValue = new Operation("");
            newValue.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
        	for(String oldValueStr: operations) {
        		Operation oldValue = new Operation("");
        		oldValue.toString(oldValueStr);
        		if (oldValue.getXmiId().equals(newValue.getXmiId())) {
        			operations.remove(oldValueStr);
        			try {
        				operations.add(index, newValue.toString());    			
        			} catch(Exception e) {
        				operations.add(newValue.toString());
        			}
            		break;
        		}
        	}
        }        
        changes.firePropertyChange(Constants.changeClassNodeOperation, null, dataArray);
    }

    public List<Attribute> getAttributes(){
    	List<Attribute> list = new ArrayList<>();
    	if (attributes != null) {
        	for (String string: attributes) {
        		Attribute attribute = new Attribute("");
        		attribute.toString(string);
        		list.add(attribute);
        	}
    	}
    	return list;
    }

    public List<Operation> getOperations(){
    	List<Operation> list = new ArrayList<>();
    	if (operations != null) {
        	for (String string: operations) {
        		Operation operation = new Operation("");
        		operation.toString(string);
        		list.add(operation);
        	}
    	}
    	return list;
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
            newCopy.setTitle(this.getTitle(),false, null);

        }
        if(this.attributes != null){
            newCopy.setAttributesStr(this.attributes);
        }
        if(this.operations != null){
            newCopy.setOperationsStr(this.operations);
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
