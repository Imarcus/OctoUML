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
import model.nodes.Attribute;
import model.nodes.ClassNode;
import model.nodes.IdentifiedTextField;
import model.nodes.Operation;
import model.nodes.Title;
import util.Constants;
import util.GlobalVariables;

import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visual representation of ClassNode class.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {
	
	private static Logger logger = LoggerFactory.getLogger(ClassNodeView.class);
	
	private List<Object> originalValues = new ArrayList<>();
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
    	    	((ClassNode)getRefNode()).setTitle(title.getText());
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
    	cmItemAddAttribute = new MenuItem("Add Attribute");
    	cmItemAddAttribute.setOnAction(event -> {
	    	addAttribute();
        });
    	MenuItem cmItemAddOperation;
    	cmItemAddOperation = new MenuItem("Add Operation");
    	cmItemAddOperation.setOnAction(event -> {
	    	addOperation();
        });
       	ContextMenu contextMenu = new ContextMenu();
    	contextMenu.getItems().addAll(cmItemAddAttribute,cmItemAddOperation);
    	// Add context menu for collaboration type UMLCollab
    	Menu cmHistory = new Menu ("History");
    	cmHistory.setVisible(false);
    	// Add context menu item "dismiss automatic merge indicator" 
  		MenuItem cmItemDismiss = new MenuItem("Dismiss automatic merge indicator");
    	cmItemDismiss.setOnAction(event -> {
	        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
	    	title.setBackground(new Background(backgroundFill));
        });
    	cmHistory.getItems().add(cmItemDismiss);
    	// Add context menu item "Clear all" 
  		MenuItem cmItemClearAll = new MenuItem("Clear all");
  		cmItemClearAll.setOnAction(event -> {
    		while (cmHistory.getItems().size() > 2) {
    	    	cmHistory.getItems().remove(cmHistory.getItems().get(2));
    		}
	        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
	    	title.setBackground(new Background(backgroundFill));
        	cmHistory.setVisible(false);
        });
    	cmHistory.getItems().add(cmItemClearAll);
    	contextMenu.getItems().addAll(cmHistory);
    	title.setContextMenu(contextMenu);

        vbox.getChildren().addAll(title, firstLine);
        
        // Store original value of title
        Title oldTitle = new Title();
        oldTitle.setText(title.getText());
        originalValues.add(oldTitle);
        
        if (node.getAttributes() != null) {
            for(String text : node.getAttributes().split("\\r?\\n")){
            	if (text.contains(";")) {
            		text = text.substring(text.indexOf(";")+1);
            	} 
            	Attribute attribute = new Attribute(text);
                vbox.getChildren().add(attribute);
                
                // Store original value of attribute
                Attribute oldAttribue = new Attribute(text);
                originalValues.add(oldAttribue);
            }
        }

        vbox.getChildren().addAll(secondLine);

        if (node.getOperations() != null) {
        	for(String text : node.getOperations().split("\\r?\\n")){
               	if (text.contains(";")) {
            		text = text.substring(text.indexOf(";")+1);
            	}         	
            	Operation operation = new Operation(text);
                vbox.getChildren().add(operation);

                // Store original value of operation
                Operation oldOperation = new Operation(text);
                originalValues.add(oldOperation);
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

    private int operationsSize() {
    	int cont = 0;
    	for(Node node: vbox.getChildren()) {
    		if (node instanceof Operation) {
    	    	cont++;
    		}
    	}
    	return cont;
    }
    
    private void createHandlesAttributesOperations(IdentifiedTextField textfield) {
    	logger.debug("createHandlesAttributesOperations()");
    	textfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
    	    public void handle(KeyEvent ke) {
    	    	if (textfield instanceof Attribute) {
        	    	((ClassNode)getRefNode()).setAttributes(extractAttributesFromVBox());
    	    	} else if (textfield instanceof Operation) {
    	    		((ClassNode)getRefNode()).setOperations(extractOperationsFromVBox());
    	    	}    	    	
    	    }
    	});
    	
    	MenuItem cmItemMoveUp;
    	cmItemMoveUp = new MenuItem("Move Up");
    	cmItemMoveUp.setUserData(textfield);
		cmItemMoveUp.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	int index = vbox.getChildren().indexOf(modifiedTextField);
    	    	if (textfield instanceof Attribute && index > 2) {
    	    		index--;
    	    		vbox.getChildren().remove(modifiedTextField);
       				vbox.getChildren().add(index,modifiedTextField);
        	    	((ClassNode)getRefNode()).setAttributes(extractAttributesFromVBox());
    	    	} else if (textfield instanceof Operation && index > (3+attributesSize())) {
    	    		index--;
    				vbox.getChildren().remove(modifiedTextField);
    				vbox.getChildren().add(index,modifiedTextField);
    	    		((ClassNode)getRefNode()).setOperations(extractOperationsFromVBox());
    	    	}  
    	    }
        });
    	MenuItem cmItemMoveDown;
    	cmItemMoveDown = new MenuItem("Move Down");    	
    	cmItemMoveDown.setUserData(textfield);
		cmItemMoveDown.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	int index = vbox.getChildren().indexOf(modifiedTextField);
    	    	if (textfield instanceof Attribute && index < (1+attributesSize())) {
    	    		index++;
    	    		vbox.getChildren().remove(modifiedTextField);
       				vbox.getChildren().add(index,modifiedTextField);
    	    		((ClassNode)getRefNode()).setAttributes(extractAttributesFromVBox());
    	    	} else if (textfield instanceof Operation && index < (2+attributesSize()+operationsSize())) {
    	    		index++;
    				vbox.getChildren().remove(modifiedTextField);
    				vbox.getChildren().add(index,modifiedTextField);
    	    		((ClassNode)getRefNode()).setOperations(extractOperationsFromVBox());
    	    	}  
    	    }
        });    		

		MenuItem cmItemDelete;
		if (textfield instanceof Attribute) {
    		cmItemDelete = new MenuItem("Delete attribute");
		} else {
    		cmItemDelete = new MenuItem("Delete operation");
    	}
    	cmItemDelete.setUserData(textfield);
    	cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	vbox.getChildren().remove(modifiedTextField);
    			if (modifiedTextField instanceof Attribute) {
        	    	String attributesfullText = extractAttributesFromVBox();
        			((ClassNode)getRefNode()).setAttributes(attributesfullText);
    			} else {
        	    	String operationsfullText = extractOperationsFromVBox();
        			((ClassNode)getRefNode()).setOperations(operationsfullText);
    	    	}
    	    }
    	});
		
       	ContextMenu contextMenu = new ContextMenu();
    	contextMenu.getItems().addAll(cmItemMoveUp,cmItemMoveDown,cmItemDelete);
    	textfield.setContextMenu(contextMenu);
    }
    
    private String extractAttributesFromVBox() {
    	logger.debug("extractAttributesFromVBox()");
    	String fullText = "";
    	for (Node node: vbox.getChildren()) {
	    	if (node instanceof Attribute) {
	    		Attribute tf = (Attribute) node;
	    		fullText = fullText + vbox.getChildren().indexOf(tf) +
    	    			";" + tf.getXmiId() + "|" + tf.getText() + System.getProperty("line.separator");
	    	}
    	}
    	return fullText;
    }
    
    private String extractOperationsFromVBox() {
    	logger.debug("extractOperationsFromVBox()");
    	String fullText = "";
    	for (Node node: vbox.getChildren()) {
	    	if (node instanceof Operation) {
	    		Operation tf = (Operation) node;
	    		fullText = fullText + vbox.getChildren().indexOf(tf) +
    	    			";" + tf.getXmiId() + "|" + tf.getText() + System.getProperty("line.separator");
	    	}
    	}
    	return fullText;
    }    
    
    public void addAttribute() {
    	logger.debug("addAttribute()");
    	Attribute textField = new Attribute("");
    	textField.setXmiId("att" + UUID.randomUUID().toString()
        		+ "_" + ((ClassNode)getRefNode()).getId());
    	createHandlesAttributesOperations(textField);
		vbox.getChildren().add(2+attributesSize(),textField);    	
    	String attributesfullText = extractAttributesFromVBox();
		((ClassNode)getRefNode()).setAttributes(attributesfullText);
    }
    
    public void addOperation() {
    	logger.debug("addOperation()");
    	Operation textField = new Operation("");
    	textField.setXmiId("oper" + UUID.randomUUID().toString()
        		+ "_" + ((ClassNode)getRefNode()).getId());
    	createHandlesAttributesOperations(textField);
		vbox.getChildren().add(textField);    	
    	String operationsFullText = extractOperationsFromVBox();
		((ClassNode)getRefNode()).setOperations(operationsFullText);
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
        	// Get new value
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
        	// If collaboration type is synchronous, simple update 
        	if (GlobalVariables.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
        		logger.debug("The type of collaboration is synchronous, performed simple update");
        		if (!title.getText().equals(newValue) ) {
        			title.setText(newValue);
        		}
    	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
        	}
        	// If collaboration type UMLCollab, carry out special treatment
        	else {
            	// If it is a local change (occurs only without unresolved conflicts),
        		// update and records the change 
            	if (evt.getNewValue() instanceof String) {
            		logger.debug("Local change, performed simple update and record of the change");
            		if (!title.getText().equals(newValue) ) {
            			title.setText(newValue);
            		}
	    	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
    		        // Records the change
    		        Title changedTitle = new Title();
    		        changedTitle.setText(newValue);
    		        Map<String, Object> map = new HashMap<String, Object>();
    		    	map.put(GlobalVariables.getUserName(), changedTitle);
    		        changedValues.put(((ClassNode)getRefNode()).getId(),map);
            	}
        		// For a remote change, check proper merge method
            	else {
            		// Set backgrounds for automatic merge and conflicts
    		        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
    		        Background backgroundDefault =  new Background(backgroundFill);
    		        backgroundFill = new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY);
    		        Background backgroundAutomaticMerge =  new Background(backgroundFill);
    		        backgroundFill = new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY);
    		        Background backgroundConflict =  new Background(backgroundFill);
    		        // Get current date and time and remote user
    		        String dateTimeUser = " (by " + dataArray[3] + " in " +
    		        		new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date()) + ")";
            		// If no previous changes were made, simple do a automatic merge
            		if (changedValues.get(((ClassNode)getRefNode()).getId()) == null) {
                		logger.debug("Remote change without previous changes, performed automatic merge");
        				// Automatic merge title
                		if (!title.getText().equals(newValue) ) {
                			title.setText(newValue);
                		}
    	    	    	((ClassNode)getRefNode()).setTitleOnly(newValue);
            			// Indicates the automatic merge
    	    	    	title.setBackground(backgroundAutomaticMerge);
        		        // Set interface for proper action
        		       	ContextMenu contextMenu = title.getContextMenu();
    	    	    	ObservableList<MenuItem> observableList = contextMenu.getItems();
      	    	  	  	for(int i = 0; i < observableList.size(); i++) {
      	    	  	  		// Get History context menu item
      	    	  	  		if(observableList.get(i).getText().equals("History")){
      	    	  	  			Menu cmHistory = (Menu) observableList.get(i);
      	      	    	  	  	cmHistory.setVisible(true);
      	    	  	  			// Add new merge history
      	    	  	  			MenuItem cmChange = new MenuItem("title merged to '" + dataArray[2] + "'" +
      	    	  	  					dateTimeUser);
      	      	    	  	  	cmHistory.getItems().add(2, cmChange);
      	      	    	  	  	break;
      	    	  	  		}
      	    	  	  	}
            		}
	        		// If previous changes were made, deal with a possible conflict
    	        	else {
    	        		// Get changes for this element
    	        		Map<String, Object> map = changedValues.get(((ClassNode)getRefNode()).getId());
    	        		// If a remote user send a new update from previous one, simply updates
        		        if (map.get(dataArray[3]) != null) {
                    		logger.debug("New remote change frow same user, update pending evaluation dispatch queue");
        		        	// TODO: Update pending evaluation dispatch queue
        		        	// Update the records of the merge
            		        Title changedTitle = new Title();
            		        changedTitle.setText(newValue);
        	        		map.put(dataArray[3],changedTitle);
        		        }
    	        		// If a remote user send a update without a previous one
        		        else {
                    		logger.debug("Totally new change, added to update pending evaluation dispatch queue");
        		        	// TODO: Add to pending evaluation dispatch queue
                			// Indicates the automatic merge
                    		title.setBackground(backgroundConflict);
            		        // Set interface for proper action
            		       	ContextMenu contextMenu = title.getContextMenu();
            		    	Menu cmChange = new Menu("Conflicting title: '" + dataArray[2] + "')" +
            		    			dateTimeUser);
            		    	MenuItem cmItemActionAprove = new MenuItem("Aprove");
            		    	cmItemActionAprove.setOnAction(event -> {
            	    	    	title.setBackground(backgroundDefault);
            		    		contextMenu.getItems().remove(cmChange);
            		        });            
            		    	MenuItem cmItemActionReject = new MenuItem("Reject");
            		    	cmItemActionReject.setOnAction(event -> {
            	    	    	title.setBackground(backgroundDefault);
            		    		contextMenu.getItems().remove(cmChange);
            		        });            
            		    	cmChange.getItems().addAll(cmItemActionAprove,cmItemActionReject);
            		    	contextMenu.getItems().addAll(cmChange);
        		        }
        	        }
	        	}
        	}
        } else if ( evt.getPropertyName().equals(Constants.changeClassNodeAttributes) ) {
        	String newValue = (String) evt.getNewValue();
        	// Check for removed attributes
        	for (int cont = 0; cont < vbox.getChildren().size(); cont++ ) {
        		Node node = vbox.getChildren().get(cont);
        		if ( node instanceof Attribute ) {
        			Attribute localTextField = (Attribute) node;
            		if (!newValue.contains(localTextField.getXmiId())) {
                        vbox.getChildren().remove(localTextField);
            		}
        		}
        	}
        	// Check for new attributes
        	for(String text : newValue.split("\\r?\\n")) {
            	String array[] = text.split(";");
            	int index = Integer.parseInt(array[0]);
            	Attribute remoteTextField = new Attribute(array[1]);
            	boolean found = false;
            	for (Node node: vbox.getChildren()) {
            		if ( node instanceof Attribute ) {
            			Attribute localTextField = (Attribute) node;
                		if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
                			found = true;
                			break;
                		}
            		}
            	}
            	if (!found) {
            		createHandlesAttributesOperations(remoteTextField);
            		vbox.getChildren().add(index,remoteTextField);
            	}            	
        	}
        	// Check for text altered or attribute moved up or down
        	for(String text : newValue.split("\\r?\\n")){
            	String array[] = text.split(";");
            	int index = Integer.parseInt(array[0]);
            	Attribute remoteTextField = new Attribute(array[1]);
            	for (Node node: vbox.getChildren()) {
            		if ( node instanceof Attribute ) {
            			Attribute localTextField = (Attribute) node;
                    	if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
                			// Update text if it was altered
                			if (!localTextField.getText().equals(remoteTextField.getText())) {
                    			localTextField.setText(remoteTextField.getText());
                			}
                			// If moved upper or down
                			if (vbox.getChildren().indexOf(localTextField) != index) {
                				vbox.getChildren().remove(localTextField);
                				vbox.getChildren().add(index,localTextField);    	
                			}
                			break;
                		}
            		}
            	}
        	}
	        if (operationsSize() > 0) {
	            secondLine.setVisible(true);
	        } else {
	            secondLine.setVisible(false);
	        }

        } else if ( evt.getPropertyName().equals(Constants.changeClassNodeOperations) ) {
        	String newValue = (String) evt.getNewValue();
        	// Check for removed operations
        	for (int cont = 0; cont < vbox.getChildren().size(); cont++ ) {
        		Node node = vbox.getChildren().get(cont);
        		if ( node instanceof Operation ) {
        			Operation localTextField = (Operation) node;
            		if (!newValue.contains(localTextField.getXmiId())) {
                        vbox.getChildren().remove(localTextField);
            		}
        		}
        	}
        	// Check for new operations
        	for(String text : newValue.split("\\r?\\n")) {
            	String array[] = text.split(";");
            	int index = Integer.parseInt(array[0]);
            	Operation remoteTextField = new Operation(array[1]);
            	boolean found = false;
            	for (Node node: vbox.getChildren()) {
            		if ( node instanceof Operation ) {
            			Operation localTextField = (Operation) node;
                		if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
                			found = true;
                			break;
                		}
            		}
            	}
            	if (!found) {
            		createHandlesAttributesOperations(remoteTextField);
            		vbox.getChildren().add(index,remoteTextField);
            	}            	
        	}
        	// Check for text altered or attribute or operation moved up or down
        	for(String text : newValue.split("\\r?\\n")){
            	String array[] = text.split(";");
            	int index = Integer.parseInt(array[0]);
            	Operation remoteTextField = new Operation(array[1]);
            	for (Node node: vbox.getChildren()) {
            		if ( node instanceof Operation ) {
            			Operation localTextField = (Operation) node;
                    	if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
                			// Update text if it was altered
                			if (!localTextField.getText().equals(remoteTextField.getText())) {
                    			localTextField.setText(remoteTextField.getText());
                			}
                			// If moved upper or down
                			if (vbox.getChildren().indexOf(localTextField) != index) {
                				vbox.getChildren().remove(localTextField);
                				vbox.getChildren().add(index,localTextField);    	
                			}
                			break;
                		}
            		}
            	}
        	}
	        if (operationsSize() > 0) {
	            secondLine.setVisible(true);
	        } else {
	            secondLine.setVisible(false);
	        }
        }
    }

	private void dismissAutomaticMerge(MenuItem cmItemChange) {
		// TODO Auto-generated method stub
		
	}
}
