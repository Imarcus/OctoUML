package view.nodes;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import model.RemoteChange;
import model.nodes.AbstractNode;
import model.nodes.Attribute;
import model.nodes.ClassNode;
import model.nodes.IdentifiedTextField;
import model.nodes.Operation;
import model.nodes.Title;
import util.Constants;
import util.GlobalVariables;
import view.edges.AbstractEdgeView;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visual representation of ClassNode class.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {
	
	private static Logger logger = LoggerFactory.getLogger(ClassNodeView.class);
	
	private Map<String, Map<String, Object>> changedValues = new HashMap<String, Map<String, Object>>();
	
    private Rectangle rectangle;

    private StackPane container;
    private VBox vbox;

    private Separator firstLine;
    private Separator secondLine;

    private Line shortHandleLine;
    private Line longHandleLine;

    private final int STROKE_WIDTH = 1;
    
    private Title title;

    public ClassNodeView(ClassNode node) {
    	super(node);
    	logger.debug("ClassNodeView()");
        //setChangeListeners();

        container = new StackPane();
        rectangle = new Rectangle();
        vbox = new VBox();
        container.getChildren().addAll(rectangle, vbox);


        initVBox();
        createRectangles();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        initLooks();

        this.getChildren().add(container);

        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());
        createHandles();
    }

    private void createRectangles(){
    	logger.debug("createRectangles()");
        ClassNode node = (ClassNode) getRefNode();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());
    }

    private void changeHeight(double height){
    	logger.debug("changeHeight()");
        setHeight(height);
        rectangle.setHeight(height);
    }

    private void changeWidth(double width){
    	logger.debug("changeWidth()");
        setWidth(width);
        rectangle.setWidth(width);
        container.setMaxWidth(width);
        container.setPrefWidth(width);

        vbox.setMaxWidth(width);
        vbox.setPrefWidth(width);
        firstLine.setMaxWidth(width);
        firstLine.setPrefWidth(width);
        secondLine.setMaxWidth(width);
        secondLine.setPrefWidth(width);

    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Attribute
    				|| node instanceof Operation
    				|| node instanceof Title) {
    			TextField tf = (TextField) node;
    			tf.setMaxWidth(width);
    			tf.setPrefWidth(width);
    		}
    	}
    }

    private void createHandles(){
    	logger.debug("createHandles()");
        shortHandleLine = new Line();
        longHandleLine = new Line();

        shortHandleLine.startXProperty().bind(rectangle.widthProperty().subtract(7));
        shortHandleLine.startYProperty().bind(rectangle.heightProperty().subtract(3));
        shortHandleLine.endXProperty().bind(rectangle.widthProperty().subtract(3));
        shortHandleLine.endYProperty().bind(rectangle.heightProperty().subtract(7));
        longHandleLine.startXProperty().bind(rectangle.widthProperty().subtract(15));
        longHandleLine.startYProperty().bind(rectangle.heightProperty().subtract(3));
        longHandleLine.endXProperty().bind(rectangle.widthProperty().subtract(3));
        longHandleLine.endYProperty().bind(rectangle.heightProperty().subtract(15));

        this.getChildren().addAll(shortHandleLine, longHandleLine);

		title.setOnKeyReleased(new EventHandler<KeyEvent>() {
    	    public void handle(KeyEvent ke) {
    	    	((ClassNode)getRefNode()).setTitle(title.getText(),false);
    	    }
    	});
        
        for(Node node: vbox.getChildren()) {
    		if (node instanceof Attribute) {
            	createHandlesAttributesOperations((Attribute) node);
    		}
    		else if (node instanceof Operation) {
            	createHandlesAttributesOperations((Operation) node);
    		}
    	}
    }

    private void initVBox(){
    	logger.debug("initVBox()");
        ClassNode node = (ClassNode) getRefNode();

        vbox.setPadding(new Insets(5, 0, 5, 0));
        vbox.setSpacing(5);

        firstLine = new Separator();
        firstLine.setMaxWidth(node.getWidth());

        secondLine = new Separator();
        secondLine.setMaxWidth(node.getWidth());

        title = new Title();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        if(node.getTitle() != null) {
            title.setText(node.getTitle());
        }
        title.setAlignment(Pos.CENTER);
        
        // Set add attribute and operation context menus
    	MenuItem cmItemAddAttribute;
    	cmItemAddAttribute = new MenuItem(GlobalVariables.getString("addAttribute"));
    	cmItemAddAttribute.setOnAction(event -> {
	    	addAttribute();
        });
    	MenuItem cmItemAddOperation;
    	cmItemAddOperation = new MenuItem(GlobalVariables.getString("addOperation"));
    	cmItemAddOperation.setOnAction(event -> {
	    	addOperation();
        });
       	ContextMenu contextMenu = new ContextMenu();
    	contextMenu.getItems().addAll(cmItemAddAttribute,cmItemAddOperation);
    	// Add context menu for collaboration type UMLCollab
    	Menu cmHistory = new Menu (GlobalVariables.getString("history"));
    	// Add context menu item "Clear all" 
  		MenuItem cmItemClearAll = new MenuItem(GlobalVariables.getString("clearAll"));
  		cmItemClearAll.setOnAction(event -> {
    		while (cmHistory.getItems().size() > 1) {
    	    	cmHistory.getItems().remove(cmHistory.getItems().get(1));
    		}
	        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
	    	title.setBackground(new Background(backgroundFill));
        });
    	cmHistory.getItems().add(cmItemClearAll);
    	contextMenu.getItems().addAll(cmHistory);
    	title.setContextMenu(contextMenu);

        vbox.getChildren().addAll(title, firstLine);
        
        if (node.getAttributes() != null) {
        	Iterator<Attribute> i = node.getAttributes().iterator();
            while (i.hasNext()) {
            	Attribute attribute = i.next();
                vbox.getChildren().add(attribute);
            }
        }

        vbox.getChildren().addAll(secondLine);

        if (node.getOperations() != null) {
        	Iterator<Operation> i = node.getOperations().iterator();
            while (i.hasNext()) {
            	Operation operation = i.next();
                vbox.getChildren().add(operation);
            }
        }

        if (operationsSize() > 0) {
            secondLine.setVisible(true);
        } else {
            secondLine.setVisible(false);
        }
    }

    private void initLooks(){
    	logger.debug("initLooks()");
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        Background background =  new Background(backgroundFill);
        StackPane.setAlignment(title, Pos.CENTER);
        title.setBackground(background);
    }

    public void setSelected(boolean selected){
    	logger.debug("setSelected()");
        if(selected){
            rectangle.setStrokeWidth(2);
            setStroke(Constants.selected_color);
        } else {
            rectangle.setStrokeWidth(1);
            setStroke(Color.BLACK);
        }
    }

    public void setStrokeWidth(double scale){
    	logger.debug("setStrokeWidth()");
        rectangle.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
    	logger.debug("setFill()");
        rectangle.setFill(p);
    }

    public void setStroke(Paint p) {
    	logger.debug("setStroke()");
        rectangle.setStroke(p);
    }

    public Bounds getBounds(){
        return container.getBoundsInParent();
    }
    
    private int attributesSize() {
    	int cont = 0;
    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Attribute) {
    	    	cont++;
    		}
    	}
    	return cont;
    }

    private List<Attribute> getAttributes() {
    	List<Attribute> list = new ArrayList<>();
    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Attribute) {
    	    	list.add((Attribute)node);
    		}
    	}
    	return list;
    }    
    
    private int operationsSize() {
    	int cont = 0;
    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Operation) {
    	    	cont++;
    		}
    	}
    	return cont;
    }

    private List<Operation> getOperations() {
    	List<Operation> list = new ArrayList<>();
    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Operation) {
    	    	list.add((Operation)node);
    		}
    	}
    	return list;
    }    
    
    private int indexOf(IdentifiedTextField tf) {
    	if (tf instanceof Attribute) {
    		return vbox.getChildren().indexOf(tf)-2;
    	} else if (tf instanceof Operation) {
    		return vbox.getChildren().indexOf(tf)-3-attributesSize();
    	}
    	return -1;
    }

    private void addAttributeOperationToVbox(int index, IdentifiedTextField newValue) {
    	if (newValue instanceof Attribute) {
    		index = index + 2;
    		try {
        		vbox.getChildren().add(index,newValue);
    		} catch(Exception e) {
        		vbox.getChildren().add(newValue);
    		}    		
    	} else if (newValue instanceof Operation) {
    		index = index + 3 + attributesSize();
    		try {
        		vbox.getChildren().add(index,newValue);
    		} catch(Exception e) {
        		vbox.getChildren().add(newValue);
    		}    		
    	}
    }    
    
    private void createHandlesAttributesOperations(IdentifiedTextField textfield) {
    	logger.debug("createHandlesAttributesOperations()");
    	textfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
    	    public void handle(KeyEvent ke) {
    	    	if (textfield instanceof Attribute) {
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(textfield), (Attribute)textfield, false);
    	    	} else if (textfield instanceof Operation) {
    	    		((ClassNode)getRefNode()).setOperation(indexOf(textfield), (Operation)textfield, false);
    	    	}    	    	
    	    }
    	});
    	
    	MenuItem cmItemMoveUp;
    	cmItemMoveUp = new MenuItem(GlobalVariables.getString("moveUp"));
    	cmItemMoveUp.setUserData(textfield);
		cmItemMoveUp.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	int index = vbox.getChildren().indexOf(modifiedTextField);
    	    	if (modifiedTextField instanceof Attribute && index > 2) {
    	    		index--;
    	    		Attribute tf = (Attribute) modifiedTextField;
    	    		vbox.getChildren().remove(tf);
       				vbox.getChildren().add(index,tf);
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(tf), tf, false);
    	    	} else if (textfield instanceof Operation && index > (3+attributesSize())) {
    	    		index--;
    	    		Operation tf = (Operation) modifiedTextField;
    	    		vbox.getChildren().remove(tf);
       				vbox.getChildren().add(index,tf);
    	    		((ClassNode)getRefNode()).setOperation(indexOf(tf), tf, false);
    	    	}  
    	    }
        });
    	MenuItem cmItemMoveDown;
    	cmItemMoveDown = new MenuItem(GlobalVariables.getString("moveDown"));    	
    	cmItemMoveDown.setUserData(textfield);
		cmItemMoveDown.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	int index = vbox.getChildren().indexOf(modifiedTextField);
    	    	if (textfield instanceof Attribute && index < (1+attributesSize())) {
    	    		index++;
    	    		Attribute tf = (Attribute) textfield;
    	    		vbox.getChildren().remove(tf);
       				vbox.getChildren().add(index,tf);
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(tf), tf, false);
    	    	} else if (textfield instanceof Operation && index < (2+attributesSize()+operationsSize())) {
    	    		index++;
    	    		Operation tf = (Operation) textfield;
    				vbox.getChildren().remove(tf);
    				vbox.getChildren().add(index,tf);
    	    		((ClassNode)getRefNode()).setOperation(indexOf(tf), tf, false);
    	    	}  
    	    }
        });    		

		MenuItem cmItemDelete;
		if (textfield instanceof Attribute) {
    		cmItemDelete = new MenuItem(GlobalVariables.getString("deleteAttribute"));
		} else {
    		cmItemDelete = new MenuItem(GlobalVariables.getString("deleteOperation"));
    	}
    	cmItemDelete.setUserData(textfield);
    	cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	vbox.getChildren().remove(modifiedTextField);
    			if (modifiedTextField instanceof Attribute) {
    				Attribute tf = (Attribute) modifiedTextField;
        			((ClassNode)getRefNode()).setAttribute(-1,tf, false);
    			} else {
    				Operation tf = (Operation) modifiedTextField;
        			((ClassNode)getRefNode()).setOperation(-1,tf, false);
    	    	}
    	    }
    	});
		
       	ContextMenu contextMenu = new ContextMenu();
    	contextMenu.getItems().addAll(cmItemMoveUp,cmItemMoveDown,cmItemDelete);
    	// Add context menu for collaboration type UMLCollab
    	Menu cmHistory = new Menu (GlobalVariables.getString("history"));
    	// Add context menu item "Clear all" 
  		MenuItem cmItemClearAll = new MenuItem(GlobalVariables.getString("clearAll"));
  		cmItemClearAll.setOnAction(event -> {
    		while (cmHistory.getItems().size() > 1) {
    	    	cmHistory.getItems().remove(cmHistory.getItems().get(1));
    		}
	        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
	    	title.setBackground(new Background(backgroundFill));
        });
    	cmHistory.getItems().add(cmItemClearAll);
    	contextMenu.getItems().addAll(cmHistory);
    	textfield.setContextMenu(contextMenu);
    }
    
    public void addAttribute() {
    	logger.debug("addAttribute()");
    	Attribute textField = new Attribute("");
    	textField.setXmiId("att" + UUID.randomUUID().toString());
    	createHandlesAttributesOperations(textField);
		vbox.getChildren().add(textField);
		((ClassNode)getRefNode()).setAttribute(indexOf(textField),textField, false);
    }
    
    public void addOperation() {
    	logger.debug("addOperation()");
    	Operation textField = new Operation("");
    	textField.setXmiId("oper" + UUID.randomUUID().toString());
    	createHandlesAttributesOperations(textField);
		vbox.getChildren().add(textField);    	
		((ClassNode)getRefNode()).setOperation(indexOf(textField),textField, false);
    }
    
    private Menu getHistoryMenu (TextField textField) {
		// Record conflict decision to history
    	ObservableList<MenuItem> list = textField.getContextMenu().getItems();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getText().equals(GlobalVariables.getString("history"))) {
				return (Menu) list.get(i);
			}
		}
		return null;
    }
    
    private String getDateTimeUserSuffixString(String userName) {
		Date currentDate = new Date();
        String dateTimeUserSuffixString = " (" + GlobalVariables.getString("by") + " " + userName +
        		" " + GlobalVariables.getString("in") + " " + new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(currentDate) + ")";
        return dateTimeUserSuffixString;
    }

    // Create conflict record
    private void recordConflict(TextField oldValue, Object newValue, String userName,
    		int indexContextMenu, int indexNewValue, Map<String, Object> map) {
        String newValueStr = null;
		if (oldValue instanceof Title) {
			newValueStr = (String) newValue;
		} else if (oldValue instanceof Attribute
				|| oldValue instanceof Operation) {
			newValueStr = ((TextField)newValue).getText();
		}
        Menu cmChange = new Menu(GlobalVariables.getString("conflictingValue") + ": '" + newValueStr + "'" +
        		getDateTimeUserSuffixString(userName));
        // Create aprove option
        MenuItem cmItemActionAprove = new MenuItem(GlobalVariables.getString("accept"));
    	cmItemActionAprove.setOnAction(event -> {
	    	// Update old value
    		if (oldValue instanceof Title) {
        		if (!oldValue.getText().equals((String)newValue) ) {
        			oldValue.setText((String)newValue);
        		}
    	    	((ClassNode)getRefNode()).setTitleOnly((String)newValue);
    		} else if (oldValue instanceof Attribute
    				|| oldValue instanceof Operation) {
        		updateAttributeOperation(indexNewValue, (IdentifiedTextField)oldValue, (IdentifiedTextField)newValue);
    		}
			// TODO: Remove conflict indication
      	  	oldValue.setStyle("-fx-text-inner-color: black;");
      	  	oldValue.getContextMenu().getItems().remove(cmChange);
    		// TODO: Remove other conflicts from pending evaluation dispatch queue
	        if (oldValue instanceof Title) {
	    		while (oldValue.getContextMenu().getItems().size() > 3) {
	    			oldValue.getContextMenu().getItems().remove(oldValue.getContextMenu().getItems().get(2));
	    		}
	        } else if (oldValue instanceof Attribute
	        		|| oldValue instanceof Operation) {
	    		while (oldValue.getContextMenu().getItems().size() > 4) {
	    			oldValue.getContextMenu().getItems().remove(oldValue.getContextMenu().getItems().get(3));
	    		}
    		}
    		// Record conflict decision to history
  			Menu cmHistory = getHistoryMenu(oldValue);
  			MenuItem cmChangeAproved = new MenuItem(cmChange.getText() +
  					". " + GlobalVariables.getString("accepted") + ".");
  	  	  	cmHistory.getItems().add(1, cmChangeAproved);
    		// Replace the records of the change from remote user to local user
    		map.remove(userName);
	        Object change = null;
	        if (oldValue instanceof Title) {
	        	change = new Title();
		        ((Title)change).setText(((String)newValue));
	        } else if (oldValue instanceof Attribute) {
	        	change = new Attribute("");
	        } else if (oldValue instanceof Operation) {
	        	change = new Operation("");
    		}
	        map.put(GlobalVariables.getUserName(),change);
        });            
        // Create reject option
    	MenuItem cmItemActionReject = new MenuItem(GlobalVariables.getString("reject"));
    	cmItemActionReject.setOnAction(event -> {
      	  	oldValue.setStyle("-fx-text-inner-color: black;");
    		oldValue.getContextMenu().getItems().remove(cmChange);
    		// Record conflict decision to history
  			Menu cmHistory = getHistoryMenu(oldValue);
  			MenuItem cmChangeRejected = new MenuItem(cmChange.getText() +
  					". " + GlobalVariables.getString("rejected") + ".");
  	  	  	cmHistory.getItems().add(1, cmChangeRejected);
    		// Remove the records of the change from remote user
    		map.remove(userName);
        });            
		cmChange.getItems().addAll(cmItemActionAprove,cmItemActionReject);
		oldValue.getContextMenu().getItems().add(indexContextMenu,cmChange);
		// Indicates the conflict
  	  	oldValue.setStyle("-fx-text-inner-color: red;");
    }
    
    private IdentifiedTextField getAttributeOperation(String xmiId) {
    	for (int i = 0; i <  vbox.getChildren().size(); i++) {
    		Node node = vbox.getChildren().get(i);
    		if ( node instanceof Attribute || node instanceof Operation ) {
    			IdentifiedTextField tf = (IdentifiedTextField) node;
        		if (tf.getXmiId().equals(xmiId)) {
                    return tf;
        		}
    		}
    	}  
    	return null;
    }
    
    public void umlCollabTitle(PropertyChangeEvent evt) {
    	// *** Get new value ***
    	String newValue;
    	String[] dataArray = null;
    	// From local change
    	if (evt.getNewValue() instanceof String) {
        	newValue = (String) evt.getNewValue();
    	}
    	// From remote change
    	else {
        	dataArray = (String[]) evt.getNewValue();
        	newValue = dataArray[2];
    	}
    	
    	// *** Get old value ***
    	Title oldValue = title;
    	
    	// *** Synchronous collaboration type *** 
    	if (GlobalVariables.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
    		logger.debug("The type of collaboration is synchronous, performed simple update");
    		// Update old value
    		if (!oldValue.getText().equals(newValue) ) {
    			oldValue.setText(newValue);
    		}
	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
    	}

    	// *** UMLCollab collaboration type ***
    	else {
        	// *** Local change ***
        	if (evt.getNewValue() instanceof String) {
        		logger.debug("Local change, performed simple update and record of the change");
        		// Update old value
        		if (!oldValue.getText().equals(newValue) ) {
        			oldValue.setText(newValue);
        		}
    	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
		        // Records the change
		        TextField change = new Title();
		        change.setText(newValue);
		        Map<String, Object> map = new HashMap<String, Object>();
		    	map.put(GlobalVariables.getUserName(), change);
		    	changedValues.put(((ClassNode)getRefNode()).getId(),map);
        	}
    		// *** Remote change ***
        	else {
        		// Get remote user name
        		String userName = dataArray[3];
        		// If no previous changes were made, simple do a automatic merge
        		if (changedValues.get(((ClassNode)getRefNode()).getId()) == null) {
            		logger.debug("Remote change without previous changes, performed automatic merge");
            		// Update old value
            		if (!oldValue.getText().equals(newValue) ) {
            			oldValue.setText(newValue);
            		}
	    	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
	  	  			// Add new merge history
	  	  			MenuItem cmChange = new MenuItem(GlobalVariables.getString("mergedTo") + " '" +
	  	  					newValue + "'" + getDateTimeUserSuffixString(userName));
	  	  			Menu cmHistory = getHistoryMenu(oldValue);
  	    	  	  	cmHistory.getItems().add(1, cmChange);
        			// Indicates the automatic merge
  	    	  	  	oldValue.setStyle("-fx-text-inner-color: green;");
        		}
        		// If previous changes were made, deal with a possible conflict
	        	else {
	        		// Get changes for this element
	        		Map<String, Object> map = changedValues.get(((ClassNode)getRefNode()).getId());
	        		// If a remote user send a new update from previous one, simply updates
    		        if (map.get(userName) != null) {
                		logger.debug("New remote change frow same user, updating conflicting pending evaluation dispatch queue");
                		for (int i = 0; i < oldValue.getContextMenu().getItems().size(); i++) {
                			if (oldValue.getContextMenu().getItems().get(i).getText().contains(userName)) {
                				oldValue.getContextMenu().getItems().remove(oldValue.getContextMenu().getItems().get(i));
                				recordConflict(oldValue, newValue, userName, i, 0,  map);
                        		break;
                			}
                		}
        		        // Records the change
        		        Title change = new Title();
        		        change.setText(newValue);
    	        		map.put(userName,change);
    		        }
	        		// If a remote user send a update without a previous one
    		        else {
                		logger.debug("Totally new change, added to conflicting pending evaluation dispatch queue");
        		        // Set interface for proper action
        				recordConflict(oldValue, newValue, userName, 2, 0, map);
                		// Update the records of the merge
        		        Title change = new Title();
        		        change.setText(newValue);
    	        		map.put(userName,change);
    		        }
    	        }
        	}
    	}
    }

    public void updateAttributeOperation(int index, IdentifiedTextField oldValue, IdentifiedTextField newValue) {
    	logger.debug("updateAttributeOperation()");
    	// Check for removed attributes
    	if (index == -1) {
    		if (oldValue != null) {
                vbox.getChildren().remove(oldValue);
    		}
    	} else {
        	// Check for new attributes and deal with text altered or attribute moved up or down
        	boolean found = false;
    		if (oldValue != null) {
            	found = true;
    			// Update text if it was altered
    			if (!oldValue.getText().equals(newValue.getText())) {
    				oldValue.setText(newValue.getText());
    			}
    			// If moved upper or down
    			if (indexOf(oldValue) != index) {
    				vbox.getChildren().remove(oldValue);
    				addAttributeOperationToVbox(index,oldValue);
    			}
    		}
        	// For a new attribute
        	if (!found) {
        		createHandlesAttributesOperations(newValue);
				addAttributeOperationToVbox(index,newValue);
        	}            	
    	}
		if (oldValue instanceof Attribute) {
	    	((ClassNode)getRefNode()).setAttributeOnly(index,(Attribute)newValue);
		} else {
	    	((ClassNode)getRefNode()).setOperationOnly(index,(Operation)newValue);
		}
    }
    
    public void umlCollabAttributeOperation(PropertyChangeEvent evt) {
    	// *** Get new value ***
    	IdentifiedTextField newValue;
    	String newValueStr;
    	String[] dataArray = null;
    	// From local change
    	if (evt.getNewValue() instanceof String) {
        	newValueStr = (String) evt.getNewValue();
    	}
    	// From remote change
    	else {
        	dataArray = (String[]) evt.getNewValue();
        	newValueStr = dataArray[2];
    	}
		int index = Integer.parseInt(newValueStr.substring(0, newValueStr.indexOf("|")));
		if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
			newValue = new Attribute("");
		} else {
			newValue = new Operation("");
		}
		newValue.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
    	
    	// *** Get old value ***
    	IdentifiedTextField oldValue = getAttributeOperation(newValue.getXmiId());
    	
    	// *** Synchronous collaboration type *** 
    	if (GlobalVariables.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
    		logger.debug("The type of collaboration is synchronous, performed simple update");
    		// Update old value
    		updateAttributeOperation(index, oldValue, newValue);
    	}

    	// *** UMLCollab collaboration type ***
    	else {
        	// *** Local change ***
        	if (evt.getNewValue() instanceof String) {
        		logger.debug("Local change, performed simple update and record of the change");
        		// Update old value
        		updateAttributeOperation(index, oldValue, newValue);
		        // Records the change
		        IdentifiedTextField change;
				if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
					change = new Attribute("");
				} else {
					change = new Operation("");
				}
				change.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
		        Map<String, Object> map = new HashMap<String, Object>();
		    	map.put(GlobalVariables.getUserName(), change);
		    	changedValues.put(newValue.getXmiId(),map);
        	}
    		// *** Remote change ***
        	else {
        		// Get remote user name
        		String userName = dataArray[3];
        		// If no previous changes were made, simple do a automatic merge
        		if (changedValues.get(newValue.getXmiId()) == null) {
            		logger.debug("Remote change without previous changes, performed automatic merge");
            		// Update old value
            		updateAttributeOperation(index, oldValue, newValue);
	  	  			// Add new merge history
	  	  			MenuItem cmChange = new MenuItem(GlobalVariables.getString("mergedTo") +
	  	  					" '" + newValue.getText() + "'" + getDateTimeUserSuffixString(userName));
	  	  			Menu cmHistory = getHistoryMenu(oldValue);
  	    	  	  	cmHistory.getItems().add(1, cmChange);
        			// Indicates the automatic merge
  	    	  	  	oldValue.setStyle("-fx-text-inner-color: green;");
        		}
        		// If previous changes were made, deal with a possible conflict
	        	else {
	        		// Get changes for this element
	        		Map<String, Object> map = changedValues.get(newValue.getXmiId());
	        		// If a remote user send a new update from previous one, simply updates
    		        if (map.get(userName) != null) {
                		logger.debug("New remote change frow same user, updating conflicting pending evaluation dispatch queue");
                		for (int i = 0; i < oldValue.getContextMenu().getItems().size(); i++) {
                			if (oldValue.getContextMenu().getItems().get(i).getText().contains(userName)) {
                				oldValue.getContextMenu().getItems().remove(oldValue.getContextMenu().getItems().get(i));
                				recordConflict(oldValue, newValue, userName, i, index, map);
                        		break;
                			}
                		}
        		        // Records the change
        		        IdentifiedTextField change;
        				if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
        					change = new Attribute("");
        				} else {
        					change = new Operation("");
        				}
        				change.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
    	        		map.put(userName,change);
    		        }
	        		// If a remote user send a update without a previous one
    		        else {
                		logger.debug("Totally new change, added to conflicting pending evaluation dispatch queue");
        		        // Set interface for proper action
        				recordConflict(oldValue, newValue, userName, 3, index, map);
                		// Update the records of the merge
        		        IdentifiedTextField change;
        				if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
        					change = new Attribute("");
        				} else {
        					change = new Operation("");
        				}
        				change.toString(newValueStr.substring(newValueStr.indexOf("|")+1));
    	        		map.put(userName,change);
    		        }
    	        }
        	}
    	}
    }
    
    public void commitChanges(){
    	logger.debug("handleMenuActionCommit()");
		Set<String> ids = changedValues.keySet();
		for (String id : ids)
		{
			if(id != null) {
				Map<String, Object> map = changedValues.get(id);
				Set<String> userNames = map.keySet();
				for (String userName : userNames)
				{
					Object newValue = map.get(userName);
			        if (newValue instanceof Title) {
	        	    	((ClassNode)getRefNode()).setTitle(title.getText(),true);
			        }
			        else if (newValue instanceof Attribute){
			        	Attribute attribute = (Attribute) getAttributeOperation(((Attribute)newValue).getXmiId());
	        	    	((ClassNode)getRefNode()).setAttribute(indexOf(attribute), attribute, true);
			        }
			        else if (newValue instanceof Operation){
			        	Operation operation = (Operation) getAttributeOperation(((Operation)newValue).getXmiId());
	        	    	((ClassNode)getRefNode()).setOperation(indexOf(operation), operation, true);
			        }
			    }
			}
		}    	
    }
    
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	logger.debug("propertyChange()");
    	
        super.propertyChange(evt);
        if (evt.getPropertyName().equals(Constants.changeNodeX)) {
            setX((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeY)) {
            setY((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeWidth)) {
            changeWidth((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeHeight)) {
            changeHeight((double) evt.getNewValue());
        } else if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
        	umlCollabTitle(evt);
        } else if ( evt.getPropertyName().equals(Constants.changeClassNodeAttribute) ) {
        	umlCollabAttributeOperation(evt);
        } else if ( evt.getPropertyName().equals(Constants.changeClassNodeOperation) ) {
        	umlCollabAttributeOperation(evt);
        }
    }

	private void dismissAutomaticMerge(MenuItem cmItemChange) {
		// TODO Auto-generated method stub
		
	}
}
