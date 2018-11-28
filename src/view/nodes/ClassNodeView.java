package view.nodes;

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

import controller.AbstractDiagramController;
import controller.TabController;
import javafx.application.Platform;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
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
import model.nodes.Attribute;
import model.nodes.ClassNode;
import model.nodes.IdentifiedTextField;
import model.nodes.Operation;
import model.nodes.Title;
import util.Constants;
import util.GlobalVariables;
import util.Change;

/**
 * Visual representation of ClassNode class.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {
	
	private static Logger logger = LoggerFactory.getLogger(ClassNodeView.class);
	
	private Map<String, Object> localChangedValues = new HashMap<String, Object>();
	private Map<String, Object> localRemovedValues = new HashMap<String, Object>();
	private Map<String, List<Change>> remoteChangedValues = new HashMap<String, List<Change>>();
	
    private Rectangle rectangle;

    private StackPane container;
    private VBox vbox;

    private Separator firstLine;
    private Separator secondLine;

    private Line shortHandleLine;
    private Line longHandleLine;

    private final int STROKE_WIDTH = 1;
    
    private Title title;
    
    private AbstractDiagramController abstractDiagramController;
    
    private boolean commited = true;

    public ClassNodeView(ClassNode node, AbstractDiagramController abstractDiagramController) {
    	super(node);
    	logger.debug("ClassNodeView()");
    	
    	this.abstractDiagramController = abstractDiagramController;
    	
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
    	    	((ClassNode)getRefNode()).setTitle(title.getText(),false, abstractDiagramController.getCollaborationType());
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
    		if (title.getContextMenu().getItems().size() == 3) {
        		title.setStyle("-fx-text-inner-color: black;");
    		}
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
    		if (index > 2 + attributesSize() || index < 2) {
    			index = 2 + attributesSize();
    		}
    		vbox.getChildren().add(index,newValue);
    	} else if (newValue instanceof Operation) {
    		index = index + 3 + attributesSize();
    		if (index > 3 + attributesSize() + operationsSize() || index < 3 + attributesSize() ) {
    			index = 3 + attributesSize() + operationsSize();
    		}
    		vbox.getChildren().add(index,newValue);
    	}
    }    
    
    private void createHandlesAttributesOperations(IdentifiedTextField textfield) {
    	logger.debug("createHandlesAttributesOperations()");
    	textfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
    	    public void handle(KeyEvent ke) {
    	    	if (textfield instanceof Attribute) {
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(textfield), (Attribute)textfield, false, abstractDiagramController.getCollaborationType());
    	    	} else if (textfield instanceof Operation) {
    	    		((ClassNode)getRefNode()).setOperation(indexOf(textfield), (Operation)textfield, false, abstractDiagramController.getCollaborationType());
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
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(tf), tf, false, abstractDiagramController.getCollaborationType());
    	    	} else if (textfield instanceof Operation && index > (3+attributesSize())) {
    	    		index--;
    	    		Operation tf = (Operation) modifiedTextField;
    	    		vbox.getChildren().remove(tf);
       				vbox.getChildren().add(index,tf);
    	    		((ClassNode)getRefNode()).setOperation(indexOf(tf), tf, false, abstractDiagramController.getCollaborationType());
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
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(tf), tf, false, abstractDiagramController.getCollaborationType());
    	    	} else if (textfield instanceof Operation && index < (2+attributesSize()+operationsSize())) {
    	    		index++;
    	    		Operation tf = (Operation) textfield;
    				vbox.getChildren().remove(tf);
    				vbox.getChildren().add(index,tf);
    	    		((ClassNode)getRefNode()).setOperation(indexOf(tf), tf, false, abstractDiagramController.getCollaborationType());
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
        			((ClassNode)getRefNode()).setAttribute(-1,tf, false, abstractDiagramController.getCollaborationType());
    			} else {
    				Operation tf = (Operation) modifiedTextField;
        			((ClassNode)getRefNode()).setOperation(-1,tf, false, abstractDiagramController.getCollaborationType());
        	        if (operationsSize() > 0) {
        	            secondLine.setVisible(true);
        	        } else {
        	            secondLine.setVisible(false);
        	        }
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
    		if (textfield.getContextMenu().getItems().size() == 4) {
    			textfield.setStyle("-fx-text-inner-color: black;");
    		}
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
		vbox.getChildren().add(attributesSize()+2,textField);
		((ClassNode)getRefNode()).setAttribute(indexOf(textField),textField, false, abstractDiagramController.getCollaborationType());
    }
    
    public void addOperation() {
    	logger.debug("addOperation()");
    	Operation textField = new Operation("");
    	textField.setXmiId("oper" + UUID.randomUUID().toString());
    	createHandlesAttributesOperations(textField);
		vbox.getChildren().add(attributesSize()+operationsSize()+3,textField);    	
		((ClassNode)getRefNode()).setOperation(indexOf(textField),textField, false, abstractDiagramController.getCollaborationType());
        secondLine.setVisible(true);
    }
    
    private Menu getHistoryMenu (TextField textField) {
		// Record conflict decision to history
    	if (textField.getContextMenu() != null) {
        	ObservableList<MenuItem> list = textField.getContextMenu().getItems();
    		for (int i = 0; i < list.size(); i++) {
    			if (list.get(i).getText().equals(GlobalVariables.getString("history"))) {
    				return (Menu) list.get(i);
    			}
    		}
    	}
		return null;
    }
    
    private String getDateTimeUserSuffixString(String userName) {
		Date currentDate = new Date();
        String dateTimeUserSuffixString = "(" + GlobalVariables.getString("by") + " " + userName +
        		" " + GlobalVariables.getString("in") + " " + new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(currentDate) + ")";
        return dateTimeUserSuffixString;
    }
    
    private String replaceAttributeOperationSubElement(Object obj, int index, String subElement) {
    	List<String> list;
    	String value;
    	if (obj instanceof Attribute) {
    		list = split((Attribute) obj);
    	} else {
    		list = split((Operation) obj);
    	}
		list.remove(index);
		list.add(index, subElement);
		
		// Adding visibility, name and arguments/type
    	value = list.get(0) + list.get(1) + list.get(2);
		// Adding return type
    	if (obj instanceof Operation) {
    		value = value + list.get(3);
    	}
    	return value;
    }
    
    private List<String> split(Attribute element) {
     	String aux = element.getText().trim();
        String visibility = "", name = "", returnType = "";
        List<String> list = new ArrayList<>();

        // Remove duplicate spaces before compare.
		while (aux.contains("  ")) {
			aux = aux.replace("  ", " ");
		}
		
        //Get visibility;
    	if (aux.contains("-")) {
    		if ( (aux.indexOf("-")+1) < aux.length() ) {
        		for(int i = aux.indexOf("-")+1; i < aux.length(); i++) {
        			if (!aux.substring(i, i+1).equals(" ")) {
        				visibility = aux.substring(0,i);
        				aux = aux.substring(i);
        				break;
        			}
        		}
    		} else {
				visibility = aux;
				aux = "";
    		}
    	} else if (aux.contains("+")) {
    		if ( (aux.indexOf("+")+1) < aux.length() ) {
        		for(int i = aux.indexOf("+")+1; i < aux.length(); i++) {
        			if (!aux.substring(i, i+1).equals(" ")) {
        				visibility = aux.substring(0,i);
        				aux = aux.substring(i);
        				break;
        			}
        		}
    		} else {
				visibility = aux;
				aux = "";
    		}
    	}
    	list.add(visibility);
    	
        //Get name and return type;
    	if (aux.contains(":")) {
        	name = aux.substring(0, aux.indexOf(":"));
        	returnType = aux.substring(aux.indexOf(":"));
    	} else {
        	name = aux;
        	returnType = "";
    	}
    	list.add(name);
		list.add(returnType);
    	return list;
    }
    
    private List<String> split(Operation element) {
     	String aux = element.getText().trim();
        String visibility = "", name = "", arguments = "", returnType = "";
        List<String> list = new ArrayList<>();

        // Remove duplicate spaces before compare.
		while (aux.contains("  ")) {
			aux = aux.replace("  ", " ");
		}
        //Get visibility
    	if (aux.contains("-")) {
    		if ( (aux.indexOf("-")+1) < aux.length() ) {
        		for(int i = aux.indexOf("-")+1; i < aux.length(); i++) {
        			if (!aux.substring(i, i+1).equals(" ")) {
        				visibility = aux.substring(0,i);
        				aux = aux.substring(i);
        				break;
        			}
        		}
    		} else {
				visibility = aux;
				aux = "";
    		}
    	} else if (aux.contains("+")) {
    		if ( (aux.indexOf("+")+1) < aux.length() ) {
        		for(int i = aux.indexOf("+")+1; i < aux.length(); i++) {
        			if (!aux.substring(i, i+1).equals(" ")) {
        				visibility = aux.substring(0,i);
        				aux = aux.substring(i);
        				break;
        			}
        		}
    		} else {
				visibility = aux;
				aux = "";
    		}
    	}
    	list.add(visibility);
    	
        //Get name
    	if (aux.contains("(")) {
        	name = aux.substring(0, aux.indexOf("("));
           	aux = aux.substring(aux.indexOf("("));
    	} else if (aux.contains(":")) {
        	name = aux.substring(0, aux.indexOf(":"));
           	aux = aux.substring(aux.indexOf(":"));
    	} else {
    		name = aux;
    		aux = "";
    	}
    	list.add(name);
    	
        //Get arguments
    	if ( aux.contains("(") ) {
    		if ( aux.contains(")") ) {
   	        	arguments = aux.substring(aux.indexOf("("), aux.indexOf(")")+1);
   	    		if ( (aux.indexOf(")")+1) < aux.length() ) {
   	    			returnType = aux.substring(aux.indexOf(")")+1);
   	    		}
    		} else {
   	        	arguments = aux.substring(aux.indexOf("("));
    		}
    	} else {
    		arguments = "";
    		returnType = aux;
    	}
    	list.add(arguments);    	
		list.add(returnType);
    	return list;
    }
    
    private String extractNameFromAttributeOperation(IdentifiedTextField element) {
        List<String> list;
    	if (element instanceof Attribute) {
        	list = split((Attribute)element);
    	} else {
        	list = split((Operation)element);
    	}
    	return list.get(1).trim();
    }
  	  	
    private IdentifiedTextField getAttributeOperation(IdentifiedTextField oldValue) {
    	// Get by xmiId
    	for (int i = 0; i <  vbox.getChildren().size(); i++) {
    		Node node = vbox.getChildren().get(i);
    		if ( node instanceof Attribute || node instanceof Operation ) {
    			IdentifiedTextField tf = (IdentifiedTextField) node;
        		if (tf.getXmiId().equals(oldValue.getXmiId())) {
                    return tf;
        		}
    		}
    	}
    	// Get by name
    	for (int i = 0; i <  vbox.getChildren().size(); i++) {
    		Node node = vbox.getChildren().get(i);
    		if ( node instanceof Attribute || node instanceof Operation ) {
    			IdentifiedTextField tf = (IdentifiedTextField) node;
        		if (extractNameFromAttributeOperation(tf)
        				.equals(extractNameFromAttributeOperation(oldValue))) {
                    return tf;
        		}
    		}
    	}  
    	return null;
    }
    
    public boolean isCommited() {
		return commited;
	}

	private void setCommited(boolean salvo) {
        // Update tab name
		commited = salvo;
		Set<Tab> keys = TabController.getTabMap().keySet();
		for (Tab tab: keys)
		{
			AbstractDiagramController value = TabController.getTabMap().get(tab);
	        if (value == abstractDiagramController) {
	        	String graphName = abstractDiagramController.getGraphModel().getName();
        		if (salvo) {
    	        	Platform.runLater(() -> tab.setText(graphName));
        		} else {
    	        	Platform.runLater(() -> tab.setText(graphName +  " (" + GlobalVariables.getString("localChangesNotSented") + ")" ));
        		}
	        }
		} 
    }

    public boolean elementUpdated(PropertyChangeEvent evt, int index, TextField oldValue, TextField newValue) {
    	logger.debug("elementUpdated()");
    	// Remove duplicate spaces and trim() before compare.
    	String oldValueStr = "", newValueStr = "";
    	if (oldValue != null) {
    		oldValueStr = oldValue.getText().trim();
    		while (oldValueStr.contains("  ")) {
    			oldValueStr = oldValueStr.replace("  ", " ");
    		}
    	}
    	// Get new value text
    	if (newValue != null) {
    		newValueStr = newValue.getText();
    	}
    	// If it is a title    	
    	if (newValue instanceof Title) {
            // Compare old value with new value
    		if ( !oldValueStr.equals(newValueStr) ) {
    			return true;
    		}
    	}
    	// It is a attribute or operation
    	else {
        	// It is a deleted value
        	if (index == -1) {
    			return true;
        	}
        	// Special case when received a attribute or operation that was deleted locally
	       	 else if (index == -2) {
    			return true;
	       	}
    		// It is a new or updated value
    		else {
            	// It is a new value
        		if (oldValue == null) {
        			return true;
        		}
            	// Is it a updated value?
            	else {
        			// Update text if it was altered
        			if ( !oldValueStr.equals(newValueStr) ) {
            			return true;
        			}
        			// If moved upper or down
        			if ( indexOf(((IdentifiedTextField)oldValue)) != index ) {
            			return true;
        			}
            	}
    		}
    	}
    	return false;
    }
    
    public String getUpdateTypeString(PropertyChangeEvent evt, int index,
    		Object oldValue, Object newValue, String id, String remoteUserName) {
    	logger.debug("elementUpdated()");
    	List<String> updateTypeStringList = new ArrayList<String>();
		ArrayList<Change> remoteChangeList = new ArrayList<>();
    	String updateTypeString = "";
    	// Remove duplicate spaces and trim() before compare.
    	String oldValueStr = "";
    	if (oldValue != null) {
    		oldValueStr = ((TextField)oldValue).getText().trim();
    		while (oldValueStr.contains("  ")) {
    			oldValueStr = oldValueStr.replace("  ", " ");
    		}
    	}
    	// If it is a title    	
    	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
    		if (!oldValueStr.equals(((TextField)newValue).getText()) ) {
    			updateTypeStringList.add(GlobalVariables.getString("updatedTo") +
    					" '" + ((TextField)newValue).getText() + "'");
    			remoteChangeList.add(new Change(Change.name, ((TextField)newValue).getText())); 
    		}
    	}
    	// It is a attribute or operation
    	else {
        	// It is a deleted value
        	if (index == -1) {
        		updateTypeStringList.add(GlobalVariables.getString("elementDeleted"));
    			remoteChangeList.add(new Change(Change.delete, null)); 
        	}
        	// Special case when received a attribute or operation that was deleted locally
        	 else if (index == -2) {
        		 updateTypeStringList.add(GlobalVariables.getString("remoteUpdateConfictsWithLocalDelete"));
     			 remoteChangeList.add(new Change(Change.remoteUpdateWithLocalDelete, newValue)); 
        	}
    		// It is a new or updated value
    		else {
            	// It is a new value
        		if (oldValue == null) {
        			updateTypeStringList.add(GlobalVariables.getString("newElement"));
                    Object[] dataArray = {index, newValue};
        			remoteChangeList.add(new Change(Change.newElement, dataArray)); 
        		}
            	// It is a updated value
            	else {
        			// Update text if it was altered
        			if (!oldValueStr.equals(((TextField)newValue).getText())) {
        				List<String> oldValueList;
        				List<String> newValueList;
        				if (oldValue instanceof Attribute) {
            				oldValueList = split((Attribute)oldValue);
            				newValueList = split((Attribute)newValue);
        				} else {
            				oldValueList = split((Operation)oldValue);
            				newValueList = split((Operation)newValue);
        				}	
        				if ( !oldValueList.get(0).equals(newValueList.get(0)) ) {
        					updateTypeStringList.add( GlobalVariables.getString("visibilityModifiedTo") +
        							" '" + newValueList.get(0) + "'" );
                			remoteChangeList.add(new Change(Change.visibility, newValueList.get(0))); 
        				}
        				if ( !oldValueList.get(1).equals(newValueList.get(1)) ) {
        					updateTypeStringList.add( GlobalVariables.getString("nameModifiedTo") +
        							" '" + newValueList.get(1) + "'" );
                			remoteChangeList.add(new Change(Change.name, newValueList.get(1))); 
        				}
        				if (oldValue instanceof Attribute) {
            				if ( !oldValueList.get(2).equals(newValueList.get(2)) ) {
            					updateTypeStringList.add( GlobalVariables.getString("typeModifiedTo") +
            							" '" + newValueList.get(2) + "'" );
                    			remoteChangeList.add(new Change(Change.type, newValueList.get(2))); 
            				}
        				} else {
            				if ( !oldValueList.get(2).equals(newValueList.get(2)) ) {
            					updateTypeStringList.add( GlobalVariables.getString("argumentsModifiedTo") +
            							" '" + newValueList.get(2) + "'" );
                    			remoteChangeList.add(new Change(Change.arguments, newValueList.get(2))); 
            				}
            				if ( !oldValueList.get(3).equals(newValueList.get(3)) ) {
            					updateTypeStringList.add( GlobalVariables.getString("returnTypeModifiedTo") +
            							" '" + newValueList.get(3) + "'" );
                    			remoteChangeList.add(new Change(Change.type, newValueList.get(3))); 
            				}
        				}
        			}
        			// If moved upper or down
        			if (indexOf(((IdentifiedTextField)oldValue)) != index) {
            			if (indexOf(((IdentifiedTextField)oldValue)) < index) {
            				updateTypeStringList.add(GlobalVariables.getString("movedDown") +
                					" " + (index-indexOf(((IdentifiedTextField)oldValue))) +
                					" " + GlobalVariables.getString("positions") );
            			} else if (indexOf(((IdentifiedTextField)oldValue)) > index) {
            				updateTypeStringList.add( GlobalVariables.getString("movedUp") +
                					" " + (indexOf(((IdentifiedTextField)oldValue))-index) +
                					" " + GlobalVariables.getString("positions") );
            			}
            			remoteChangeList.add(new Change(Change.moved, index)); 
        			}
            	}
    		}
    	}
    	
    	if (updateTypeStringList.size() > 0) {
        	updateTypeString = updateTypeStringList.get(0);
        	for(int cont = 1; cont < updateTypeStringList.size()-1; cont++) {
        		updateTypeString = updateTypeString + ", " + updateTypeStringList.get(cont);
        	}
        	if (updateTypeStringList.size() > 1) {
            	updateTypeString = updateTypeString + " " + GlobalVariables.getString("and") + " " +
            			updateTypeStringList.get(updateTypeStringList.size()-1);
        	}
			remoteChangedValues.put(id + "|" + remoteUserName, remoteChangeList);
    	} else {
        	logger.warn("No update string found.");
    	}
    	return updateTypeString;
    }

    public boolean updateTextField(PropertyChangeEvent evt, int index, Object oldValue, Object newValue) {
    	logger.debug("updateAttributeOperation()");
    	boolean updated = false;
    	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
    		if (!((TextField)oldValue).getText().equals(((TextField)newValue).getText()) ) {
    			((TextField)oldValue).setText(((TextField)newValue).getText());
    			updated = true;
    	    	logger.debug("old value updated");
    		}
    	} else {
        	// It is a deleted value
    		if (index == -1 || index == -2 || oldValue == null) {
            	if (index == -1) {
                    vbox.getChildren().remove(oldValue);
        			updated = true;
        	    	logger.debug("old removed");
            	}
            	// Special case when received a attribute or operation that was deleted locally and now rejected
            	else if (index == -2) {
                    vbox.getChildren().remove(newValue);
        			updated = true;
        	    	logger.debug("new value removed");
            	}
            	// It is a new value
                else if (oldValue == null) {
            		createHandlesAttributesOperations(((IdentifiedTextField)newValue));
    				addAttributeOperationToVbox(index,((IdentifiedTextField)newValue));
        			updated = true;
        	    	logger.debug("new value created");
        		}
            	if (evt.getPropertyName().equals(Constants.changeClassNodeOperation)) {
                    if (operationsSize() > 0) {
                        secondLine.setVisible(true);
                    } else {
                        secondLine.setVisible(false);
                    }
            	}
    		}
        	// It is a updated value
    		else {
    			// Update text if it was altered
    			if (!((TextField)oldValue).getText().equals(((TextField)newValue).getText())) {
    				((TextField)oldValue).setText(((TextField)newValue).getText());
        			updated = true;
        	    	logger.debug("old value updated");
    			}
    			// If moved up or down
    			if (indexOf(((IdentifiedTextField)oldValue)) != index) {
    				vbox.getChildren().remove(oldValue);
    				addAttributeOperationToVbox(index,(IdentifiedTextField)oldValue);
        			updated = true;
        	    	logger.debug("old value moved up or down");
    			}
    		}
    	}
    	return updated;
    }
    
    public boolean updateTextField(PropertyChangeEvent evt, TextField oldValue, String id, String remoteUserName) {
    	logger.debug("updateAttributeOperation()");
    	boolean updated = false;
    	List<Change> remoteChangesList = remoteChangedValues.get(id + "|" + remoteUserName);
    	if (remoteChangesList != null || remoteChangesList.size() > 0) {
        	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
        			oldValue.setText((String)remoteChangesList.get(0).getChange());
        			updated = true;
        	} else {
            	// It is a deleted value
        		if (remoteChangesList.get(0).getChangeType().equals(Change.delete)) {
                    vbox.getChildren().remove(oldValue);
        			updated = true;
        		}
            	// Special case when received a attribute or operation that was deleted locally and now rejected
            	else if (remoteChangesList.get(0).getChangeType().equals(Change.remoteUpdateWithLocalDelete)) {
                    vbox.getChildren().remove(remoteChangesList.get(0).getChange());
        			updated = true;
        	    	logger.debug("new value removed");
            	}
            	// It is a new value
                else if (remoteChangesList.get(0).getChangeType().equals(Change.newElement)) {
                	Object[] dataArray = (Object[]) remoteChangesList.get(0).getChange(); 
            		createHandlesAttributesOperations((IdentifiedTextField) dataArray[1]);
    				addAttributeOperationToVbox((int) dataArray[0], (IdentifiedTextField) dataArray[1]);
        			updated = true;
        	    	logger.debug("new value created");
        		}
            	// It is a updated value
        		else {
        			for(Change change: remoteChangesList) {
            			// Update text if it was altered
            			if ( change.getChangeType().equals(Change.visibility) ) {
            				((TextField)oldValue).setText( replaceAttributeOperationSubElement(oldValue, 0, (String)change.getChange()) );
                			updated = true;
            			}
            			else if ( change.getChangeType().equals(Change.name) ) {
            				((TextField)oldValue).setText( replaceAttributeOperationSubElement(oldValue, 1, (String)change.getChange()) );
                			updated = true;
            			}
            			else if ( change.getChangeType().equals(Change.arguments) ) {
            				((TextField)oldValue).setText( replaceAttributeOperationSubElement(oldValue, 2, (String)change.getChange()) );
                			updated = true;
            			}
            			else if ( change.getChangeType().equals(Change.type) ) {
            				if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
                				((TextField)oldValue).setText( replaceAttributeOperationSubElement(oldValue, 2, (String)change.getChange()) );
            				} else {
                				((TextField)oldValue).setText( replaceAttributeOperationSubElement(oldValue, 3, (String)change.getChange()) );
            				}
                			updated = true;
            			}
            			else if ( change.getChangeType().equals(Change.moved) &&
            					(indexOf(((IdentifiedTextField)oldValue)) != ((int)change.getChange())) ) {
            				vbox.getChildren().remove(oldValue);
            				addAttributeOperationToVbox((int)change.getChange(),(IdentifiedTextField)oldValue);
                			updated = true;
            			}
        			}
        		}
            	if (evt.getPropertyName().equals(Constants.changeClassNodeOperation)) {
                    if (operationsSize() > 0) {
                        secondLine.setVisible(true);
                    } else {
                        secondLine.setVisible(false);
                    }
            	}
        	}
    	}
    	return updated;
    }
    
    public boolean updateModel(PropertyChangeEvent evt, int index, Object oldValue, Object newValue) {
    	logger.debug("notifyModel()");
    	boolean updated = false;
    	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
	    	((ClassNode)getRefNode()).setTitleOnly(((TextField)newValue).getText());
    	} else {
    		if (newValue instanceof Attribute) {
    	    	((ClassNode)getRefNode()).setAttributeOnly(index,(Attribute)newValue);
    		} else {
    	    	((ClassNode)getRefNode()).setOperationOnly(index,(Operation)newValue);
    		}
    	}
    	return updated;
    }
    
    // Record change
    private void recordChange(Object newValue, boolean removed, String oldId) {
        Object change;
        String id;
        
        // If old id is provided, record change for new id only if it was recorded for old id
        if (oldId != null && !(oldId.isEmpty())) {
        	if (localChangedValues.get(oldId) == null &&
        			localRemovedValues.get(oldId) == null) {
        		return;       	
        	} else {
        		localChangedValues.remove(oldId);
        		localRemovedValues.remove(oldId);
        	}
        }
        
    	if (newValue instanceof Title) {
	        id = ((ClassNode)getRefNode()).getId();
    	} else {
			id = ((IdentifiedTextField)newValue).getXmiId();
    	}
    	if (removed) {
    		localChangedValues.remove(id);
    		localRemovedValues.put(id, newValue);
    	} else {
        	localChangedValues.put(id, newValue);
    		localRemovedValues.remove(id);
    	}
    }
    
    private void removeRemoteChangesFromSameUser(TextField oldValue, String userName) {
		if (oldValue != null) {
        	for (int i = 0; i < oldValue.getContextMenu().getItems().size(); i++) {
    			if (oldValue.getContextMenu().getItems().get(i).getText().contains(userName)) {
		        	logger.debug("New remote change frow same user, removing old value from pending evaluation dispatch queue");
    				oldValue.getContextMenu().getItems().remove(oldValue.getContextMenu().getItems().get(i));
    			}
    		}
		}
    }

	private void clearPendingEvaluationDispatchQueue(TextField oldValue) {
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
	}
	
	private void removeConflictIndicationWhenEmptyPendingEvaluationDispatchQueue(TextField oldValue) {
		if (oldValue instanceof Title) {
			if (oldValue.getContextMenu().getItems().size() == 3) {
				oldValue.setStyle("-fx-text-inner-color: black;");
			}
		} else {
			if (oldValue.getContextMenu().getItems().size() == 4) {
				oldValue.setStyle("-fx-text-inner-color: black;");
			}
		}
	}
	
    
    private void RemoteChangeToLocalChangeConflict(PropertyChangeEvent evt, TextField oldValue, TextField newValue,
    		int index, String id, String remoteUserName) {
    	// Remove remote changes from same user from conflicting pending evaluation dispatch queue
    	removeRemoteChangesFromSameUser(oldValue, remoteUserName);
    	// Get conflict string
        Menu cmChange = new Menu(GlobalVariables.getString("conflict") + ": " +
        		getUpdateTypeString(evt, index, oldValue, newValue, id, remoteUserName) + " " +
        		getDateTimeUserSuffixString(remoteUserName));
    
		// Create conflict record
        if (newValue instanceof Title) {
    		((TextField)oldValue).getContextMenu().getItems().add(2,cmChange);
        } else {
    		((TextField)oldValue).getContextMenu().getItems().add(3,cmChange);
        }		            
        // Create aprove option
        MenuItem cmItemActionAprove = new MenuItem(GlobalVariables.getString("accept"));
        cmItemActionAprove.setUserData(index);
    	cmItemActionAprove.setOnAction(event -> {
	    	// Update old value
    		updateTextField(evt, oldValue, id, remoteUserName);
    		// Update model
    		updateModel(evt, (int)cmItemActionAprove.getUserData(), oldValue, newValue);
    		// Clear pending evaluation dispatch queue
    		clearPendingEvaluationDispatchQueue(oldValue);
    		// Record conflict decision to history
  			Menu cmHistory = getHistoryMenu(((TextField)oldValue));
  			MenuItem cmChangeAproved = new MenuItem(cmChange.getText() +
  					". " + GlobalVariables.getString("accepted") + ".");
  	  	  	cmHistory.getItems().add(1, cmChangeAproved);
      		// Records the change
    		if ((int)cmItemActionAprove.getUserData() == -1) {
    			if (oldValue != null) {
                	recordChange(oldValue, true, null);
    			} else {
                	recordChange(newValue, true, null);
    			}
    		} else {
    			if (oldValue != null) {
                	recordChange(oldValue, false, null);
    			} else {
                	recordChange(newValue, false, null);
    			}
    		}		      	  	  	
			// Remove conflict indication
    		removeConflictIndicationWhenEmptyPendingEvaluationDispatchQueue(oldValue);
    		setCommited(false);
        });            
        // Create reject option
    	MenuItem cmItemActionReject = new MenuItem(GlobalVariables.getString("reject"));
    	cmItemActionReject.setUserData(index);
    	cmItemActionReject.setOnAction(event -> {
    		// Remove conflict from pending evaluation dispatch queue
    		((TextField)oldValue).getContextMenu().getItems().remove(cmChange);
    		// Record conflict decision to history
  			Menu cmHistory = getHistoryMenu(((TextField)oldValue));
  			MenuItem cmChangeRejected = new MenuItem(cmChange.getText() +
  					". " + GlobalVariables.getString("rejected") + ".");
  	  	  	cmHistory.getItems().add(1, cmChangeRejected);
			// Remove conflict indication
    		removeConflictIndicationWhenEmptyPendingEvaluationDispatchQueue(oldValue);
    		setCommited(false);
        });        
		cmChange.getItems().addAll(cmItemActionAprove,cmItemActionReject);
		// Indicates the conflict
		if (oldValue != null) {
    		((TextField)oldValue).setStyle("-fx-text-inner-color: red;");
		} else if (newValue != null) {
    		((TextField)newValue).setStyle("-fx-text-inner-color: red;");
		}
    }
    
    public void umlCollab(PropertyChangeEvent evt) {
    	// *** Get new value ***
    	int index = 0;
		String newValueStr, remoteUserName = null;
    	String[] dataArray = null;
    	Object newValue;
		if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
			newValue = new Title();
		} else if (evt.getPropertyName().equals(Constants.changeClassNodeAttribute)) {
			newValue = new Attribute("");
    	} else {
			newValue = new Operation("");
    	}
    	// From local change
    	if (evt.getNewValue() instanceof String) {
    		newValueStr = (String) evt.getNewValue();
    	}
    	// From remote change
    	else {
    		dataArray = (String[]) evt.getNewValue();
    		newValueStr = (String) dataArray[2];
    		// Get remote user name
    		remoteUserName = dataArray[3];
    	}
		if (newValue instanceof Title) {
			((TextField)newValue).setText(newValueStr);
		} else { 
    		index = Integer.parseInt(newValueStr.substring(0, newValueStr.indexOf("|")));
    		((IdentifiedTextField)newValue).toString(newValueStr.substring(newValueStr.indexOf("|")+1));
    		newValueStr = ((IdentifiedTextField)newValue).getText();
    	}
    	
    	// *** Get old value ***
    	Object oldValue;
    	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) { 
        	oldValue = title;
    	} else {
    		// Get old value by id is already exist
        	oldValue = getAttributeOperation((IdentifiedTextField)newValue);
    	} 
    	
    	// *** Synchronous collaboration type *** 
    	if (abstractDiagramController.getCollaborationType().equals(Constants.collaborationTypeSynchronous)) {
    		logger.debug("The type of collaboration is synchronous, performed simple update");
    		// Update old value
    		updateTextField(evt, index, oldValue, newValue);
    		updateModel(evt, index, oldValue, newValue);
    	}
    	
    	// *** UMLCollab collaboration type ***
    	else {
    		
        	// *** Local change ***
        	if (evt.getNewValue() instanceof String) {
        		logger.debug("Local change, update already done, only updating model and recording the change");
        		// Update model
        		updateModel(evt, index, oldValue, newValue);
        		// Records the change
        		if (index == -1) {
        			if (oldValue != null) {
                    	recordChange(oldValue, true, null);
        			} else {
                    	recordChange(newValue, true, null);
        			}
        		} else {
        			if (oldValue != null) {
                    	recordChange(oldValue, false, null);
        			} else {
                    	recordChange(newValue, false, null);
        			}
        		}
        		setCommited(false);
        	}
        	
    		// *** Remote change ***
        	else {

        		// Get proper id for the object
        		String id;
            	if (evt.getPropertyName().equals(Constants.changeNodeTitle)) {
            		id = ((ClassNode)getRefNode()).getId();
            	} else {
            		id = ((IdentifiedTextField)newValue).getXmiId();
            	}

            	            	
        		// *** NO CHANGES, REMOTE ELEMENT EQUALS LOCAL ELEMENT ***
	        	if (!elementUpdated(evt, index, (TextField)oldValue, (TextField)newValue)) {
    	    		logger.info(abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'" +
    	    				"\nNo changes, remote element equals local element.");
    	    		
    	    		// Only remove conflicts if it is not case of a remote change with a local delete
    	    		if (localRemovedValues.get(id) == null) {
    	    			boolean oldConflictsRemoved = false;
    		        	// Remove remote changes from same user from conflicting pending evaluation dispatch queue
    	        		if (oldValue != null) {
    			        	for (int i = 0; i < ((TextField)oldValue).getContextMenu().getItems().size(); i++) {
    		        			if (((TextField)oldValue).getContextMenu().getItems().get(i).getText().contains(remoteUserName)) {
    		        	    		logger.debug("New remote change frow same user, removing old value from pending evaluation dispatch queue");
    		        	    		oldConflictsRemoved = true;
    		    	        		// Record conflict decision to history
    		    	      			Menu cmHistory = getHistoryMenu(((TextField)oldValue));
    		    	      			MenuItem cmChange = new MenuItem(((TextField)oldValue).getContextMenu().getItems().get(i).getText() +
    		    	      					". " + GlobalVariables.getString("discardedRemoteVersionMatchsLocalVersion") + ".");
    		    	      	  	  	cmHistory.getItems().add(1, cmChange);
    		    		        	// Removing old value from pending evaluation dispatch queue
    		    	      	  	  	((TextField)oldValue).getContextMenu().getItems().remove(((TextField)oldValue).getContextMenu().getItems().get(i));
    		        			}
    		        		}
    	        		}
    	    			// Remove conflict indication
    	        		// TODO: When it is green, should not be removed the conflict indication
    	        		if (oldConflictsRemoved) {
        	        		removeConflictIndicationWhenEmptyPendingEvaluationDispatchQueue((TextField)oldValue);	        		
    	        		}
    	    		}
    	    		
	            	// If old value id differ from newValue id then the new value have the same
	            	// name of old value. In this case only a id update is needed
	            	if (oldValue != null &&
	            			(oldValue instanceof Attribute || oldValue instanceof Operation)  &&
	            			(!((IdentifiedTextField)oldValue).getXmiId().equals(((IdentifiedTextField)newValue).getXmiId())) ) {
    		        	logger.info("Remote new element name matchs local element name without changes.");
    		        	String oldId = ((IdentifiedTextField)oldValue).getXmiId();
	            		((IdentifiedTextField)oldValue).setXmiId(((IdentifiedTextField)newValue).getXmiId());
	            		// Replace the record of the change of old id for new one
	            		if (index == -1) {
	                    	recordChange(oldValue, true, oldId);
	            		} else {
	                    	recordChange(oldValue, false, oldId);
	            		}
	            	}	        		
        		}
        		
            	// CONFLICT, REMOTE NEW ELEMENT NAME MATCHS LOCAL ELEMENT NAME WITH CHANGES
        		else if (oldValue != null &&
            			(oldValue instanceof Attribute || oldValue instanceof Operation)  &&
            			(!((IdentifiedTextField)oldValue).getXmiId().equals(((IdentifiedTextField)newValue).getXmiId())) ) {
    	    		logger.info(abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'" +
    	    				"\nConflict, remote new element name matchs local element name with changes.");
		        	String oldId = ((IdentifiedTextField)oldValue).getXmiId();
            		((IdentifiedTextField)oldValue).setXmiId(((IdentifiedTextField)newValue).getXmiId());
            		// Replace the record of the change of old id for new one
            		if (index == -1) {
                    	recordChange(oldValue, true, oldId);
            		} else {
                    	recordChange(oldValue, false, oldId);
            		}
            		RemoteChangeToLocalChangeConflict(evt, (TextField) oldValue, (TextField) newValue, index, id, remoteUserName);
            	}

        		// CONFLICT, REMOTE CHANGE WITH LOCAL DELETE
        		else if (localRemovedValues.get(id) != null && index != -1) {
    	    		logger.info(abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'" +
    	    				"\nConflict, remote change with local delete");
		            // We need to recreate removed element again
		        	if (!evt.getPropertyName().equals(Constants.changeNodeTitle)) {
                		createHandlesAttributesOperations(((IdentifiedTextField)newValue));
        				addAttributeOperationToVbox(index,((IdentifiedTextField)newValue));
	  	        		// Records the change
	                	recordChange(newValue, true, null);
		        	}
	    	    	// Get conflict string
		            Menu cmChange = new Menu(GlobalVariables.getString("conflict") + ": " +
		            		getUpdateTypeString(evt, -2, oldValue, newValue, id, remoteUserName) + " " +
		            		getDateTimeUserSuffixString(remoteUserName));
	        		// Create conflict record
		            if (newValue instanceof Title) {
			    		((TextField)newValue).getContextMenu().getItems().add(2,cmChange);
		            } else {
			    		((TextField)newValue).getContextMenu().getItems().add(3,cmChange);
		            }		            
		            // Create aprove option
		            MenuItem cmItemActionAprove = new MenuItem(GlobalVariables.getString("accept"));
		            cmItemActionAprove.setUserData(index);
		        	cmItemActionAprove.setOnAction(event -> {
		        		// Remove conflict from pending evaluation dispatch queue
		        		((TextField)newValue).getContextMenu().getItems().remove(cmChange);
		        		// Record conflict decision to history
		      			Menu cmHistory = getHistoryMenu(((TextField)newValue));
		      			MenuItem cmChangeRejected = new MenuItem(cmChange.getText() +
		      					". " + GlobalVariables.getString("accepted") + ".");
		      	  	  	cmHistory.getItems().add(1, cmChangeRejected);
	  	        		// Records the change
	                	recordChange(newValue, false, null);
		    			// Remove conflict indication
		        		removeConflictIndicationWhenEmptyPendingEvaluationDispatchQueue((TextField)newValue);
		        		setCommited(false);
		            });            
		            // Create reject option
		        	MenuItem cmItemActionReject = new MenuItem(GlobalVariables.getString("reject"));
		        	cmItemActionReject.setOnAction(event -> {
		    	    	// Remove new value
		        		updateTextField(evt, -2, oldValue, newValue);
	  	        		// Records the change
	                	recordChange(newValue, true, null);
		        		setCommited(false);
		            });        
		    		cmChange.getItems().addAll(cmItemActionAprove,cmItemActionReject);
		    		((TextField)newValue).setStyle("-fx-text-inner-color: red;");
	        	}
	        	
        		// SIMPLE MERGE, REMOTE CHANGE WITHOUT LOCAL CHANGES
        		else if (localChangedValues.get(id) == null && localRemovedValues.get(id) == null  && index != -1) {
    	    		logger.info(abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'" +
    	    				"\nSimple merge, remote change without local change");
	    	    	// Get merge string
	            	MenuItem cmChange = new MenuItem(GlobalVariables.getString("merged") + ": " +
		            		getUpdateTypeString(evt, index, oldValue, newValue, id, remoteUserName) + " " +
		            		getDateTimeUserSuffixString(remoteUserName));
            		// Update old value
        			updateTextField(evt, index, oldValue, newValue);
        			// Update model
            		updateModel(evt, index, oldValue, newValue);
	    	    	// Add new merge history
	            	Menu cmHistory = null;
  	    	  	  	if (oldValue != null) {
  		  	  			cmHistory = getHistoryMenu((TextField)oldValue);
  	    	  	  	} else if (newValue != null) {
  		  	  			cmHistory = getHistoryMenu((TextField)newValue);
  	    	  	  	}
  	    	  	  	if (cmHistory != null) {
  		  	  			cmHistory.getItems().add(1, cmChange);
  	    	  	  	}
  	    	  	  	// Indicates the automatic merge
  	    	  	  	if (oldValue != null) {
  	  	    	  	  	((TextField)oldValue).setStyle("-fx-text-inner-color: green;");
  	    	  	  	} else if (newValue != null) {
  	  	    	  	  	((TextField)newValue).setStyle("-fx-text-inner-color: green;");
  	    	  	  	}
        		}
        		
        		// CONFLICT, REMOTE DELETE WITH/WITHOUT LOCAL CHANGE
	        	else if (index == -1) {
	        		String logMessage = abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'";
        			if (localChangedValues.get(id) != null) {
	    	    		logger.info(logMessage + "\nRemote delete with local change, recording conflict");
        			} else {
	    	    		logger.info(logMessage + "\nRemote delete without local change, recording conflict");
        			}
	        		// Old value will be null if the a previous conflict was accepted
	        		if (oldValue != null) {
		        		RemoteChangeToLocalChangeConflict(evt, (TextField) oldValue, (TextField) newValue, index, id, remoteUserName);
	        		}
	        	}

        		// CONFLICT, REMOTE CHANGE WITH LOCAL CHANGE
	        	else if (localChangedValues.get(id) != null) {
	        		String logMessage = abstractDiagramController.getUserName() +
    	    				":\nReceived new value: '" + ((TextField)newValue).getText() + 	"' from " + remoteUserName +
    	    				"\nOld value: '" + oldValue + "'";
    	    		logger.info(logMessage + "\nRemote change with Local change, recording conflict");
	        		RemoteChangeToLocalChangeConflict(evt, (TextField) oldValue, (TextField) newValue, index, id, remoteUserName);
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
        } else if (evt.getPropertyName().equals(Constants.changeNodeTitle)
        		|| evt.getPropertyName().equals(Constants.changeClassNodeAttribute)
        		|| evt.getPropertyName().equals(Constants.changeClassNodeOperation)) {
        	umlCollab(evt);
        }
    }
    
    // Transmit all changes made by local user or from a remote change but accepted by local user
	public boolean commitChanges(){
    	logger.debug("handleMenuActionCommit()");
    	boolean success = false;
    	if (!commited) {
        	// Removed duplicate spaces and trim() before send changes
        	for (int i = 0; i <  vbox.getChildren().size(); i++) {
        		Node node = vbox.getChildren().get(i);
        		if (node instanceof Title
        				|| node instanceof Attribute
        				|| node instanceof Operation) {
            		TextField tf = (TextField) node;
            		tf.setText(tf.getText().trim());
            		while (tf.getText().contains("  ")) {
            			tf.setText(tf.getText().replace("  ", " "));
            		}
        		}
        	}
        	// Send changed values
    		Set<String> ids = localChangedValues.keySet();
    		for (String id : ids)
    		{
    			Object newValue = localChangedValues.get(id);
    	        if (newValue instanceof Title) {
        	    	((ClassNode)getRefNode()).setTitle(((Title)newValue).getText(),true, abstractDiagramController.getCollaborationType());
    	        }
    	        else if (newValue instanceof Attribute){
    	        	Attribute attribute = (Attribute) getAttributeOperation((Attribute)newValue);
        	    	((ClassNode)getRefNode()).setAttribute(indexOf(attribute), attribute, true, abstractDiagramController.getCollaborationType());
    	        }
    	        else if (newValue instanceof Operation){
    	        	Operation operation = (Operation) getAttributeOperation((Operation)newValue);
        	    	((ClassNode)getRefNode()).setOperation(indexOf(operation), operation, true, abstractDiagramController.getCollaborationType());
    	        }
    		}
        	// Send removed values
    		ids = localRemovedValues.keySet();
    		for (String id : ids)
    		{
    			Object newValue = localRemovedValues.get(id);
    	        if (newValue instanceof Attribute){
    	        	Attribute attribute = (Attribute) newValue;
        	    	((ClassNode)getRefNode()).setAttribute(-1, attribute, true, abstractDiagramController.getCollaborationType());
    	        }
    	        else if (newValue instanceof Operation){
    	        	Operation operation = (Operation) newValue;
        	    	((ClassNode)getRefNode()).setOperation(-1, operation, true, abstractDiagramController.getCollaborationType());
    	        }
    		}
    		setCommited(true);
    		success = true;
    	}
		return success;
    }

	private void dismissAutomaticMerge(MenuItem cmItemChange) {
		// TODO Auto-generated method stub
		
	}
}
