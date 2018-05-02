package view.nodes;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.IdentifiedTextField;
import model.nodes.ClassNode;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Visual representation of ClassNode class.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {

    private TextField title;
    private List<IdentifiedTextField> attributes;
    private List<IdentifiedTextField> operations;

    private Rectangle rectangle;

    private StackPane container;
    private StackPane titlePane;
    private VBox vbox;

    private Separator firstLine;
    private Separator secondLine;

    private Line shortHandleLine;
    private Line longHandleLine;

    private final int STROKE_WIDTH = 1;

    public ClassNodeView(ClassNode node) {
        super(node);
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
        ClassNode node = (ClassNode) getRefNode();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());
    }

    private void changeHeight(double height){
        setHeight(height);
        rectangle.setHeight(height);
    }

    private void changeWidth(double width){
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

        title.setMaxWidth(width);
        title.setPrefWidth(width);

    	TextField textField;
    	Iterator i = attributes.iterator();
        while (i.hasNext()) {
        	textField = (TextField) i.next();
        	textField.setMaxWidth(width);
        	textField.setPrefWidth(width);
        }
        i = operations.iterator();
        while (i.hasNext()) {
        	textField = (TextField) i.next();
        	textField.setMaxWidth(width);
        	textField.setPrefWidth(width);
        }
    }

    private void createHandles(){
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

    	title.textProperty().addListener(new ChangeListener<String>() {
    	    @Override
    	    public void changed(ObservableValue<? extends String> observable,
    	            String oldValue, String newValue) {
    	    	((ClassNode)getRefNode()).setTitle(newValue);
    	        System.out.println("Title changed to " + newValue + ")\n");
    	    }
    	});
    	for (IdentifiedTextField textField: attributes) {
        	createHandlesAttributesOperations(textField, attributes, "attribute");
    	}
    	for (IdentifiedTextField textField: operations) {
        	createHandlesAttributesOperations(textField, operations, "operation");
    	}
    }

    private void initVBox(){
        ClassNode node = (ClassNode) getRefNode();

        vbox.setPadding(new Insets(5, 0, 5, 0));
        vbox.setSpacing(5);

        titlePane = new StackPane();

        firstLine = new Separator();
        firstLine.setMaxWidth(node.getWidth());

        secondLine = new Separator();
        secondLine.setMaxWidth(node.getWidth());

        title = new TextField();
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        if(node.getTitle() != null) {
            title.setText(node.getTitle());
        }
        title.setAlignment(Pos.CENTER);
        
        attributes = new ArrayList<>();
        if (node.getAttributes() != null) {
            for(String text : node.getAttributes().split("\\r?\\n")){
            	if (text.contains(";")) {
            		text = text.substring(text.indexOf(";")+1);
            	} 
            	IdentifiedTextField textfield = new IdentifiedTextField(text);
            	textfield.setFont(Font.font("Verdana", 10));
            	attributes.add(textfield);
            }
        }
        operations = new ArrayList<>();
        if (node.getOperations() != null) {
            for(String text : node.getOperations().split("\\r?\\n")){
               	if (text.contains(";")) {
            		text = text.substring(text.indexOf(";")+1);
            	}         	
            	IdentifiedTextField textfield = new IdentifiedTextField(text);
            	textfield.setFont(Font.font("Verdana", 10));
            	operations.add(textfield);
            }
        }

        if(operations.isEmpty()){
            secondLine.setVisible(false);
        }

        titlePane.getChildren().add(title);
        vbox.getChildren().addAll(titlePane, firstLine);
        vbox.getChildren().addAll(attributes);
        vbox.getChildren().addAll(secondLine);
        vbox.getChildren().addAll(operations);
    }

    private void initLooks(){
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);
        StackPane.setAlignment(title, Pos.CENTER);
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        Background background =  new Background(backgroundFill);
        title.setBackground(background);
        for (TextField tf: attributes) {
        	tf.setPadding(new Insets(0));
        	tf.setBackground(background);
        }
        for (TextField tf: operations) {
        	tf.setPadding(new Insets(0));
        	tf.setBackground(background);
        }
    }

    public void setSelected(boolean selected){
        if(selected){
            rectangle.setStrokeWidth(2);
            setStroke(Constants.selected_color);
        } else {
            rectangle.setStrokeWidth(1);
            setStroke(Color.BLACK);
        }
    }

    public void setStrokeWidth(double scale){
        rectangle.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
        rectangle.setFill(p);
    }

    public void setStroke(Paint p) {
        rectangle.setStroke(p);
    }

    public Bounds getBounds(){
        return container.getBoundsInParent();
    }
    
    private void createHandlesAttributesOperations(IdentifiedTextField tf,
    		List<IdentifiedTextField> list, String type) {
    	
    	tf.setOnKeyReleased(new EventHandler<KeyEvent>() {
    	    public void handle(KeyEvent ke) {
    	    	String fullText = "";
    	    	for (IdentifiedTextField tf: list) {
        	    	fullText = fullText + list.indexOf(tf) + ";" + tf.getXmiId() + "|" + tf.getText() + System.getProperty("line.separator");
    	    	}    	    	
    	    	if (type.equals("attribute")) {
    	    		((ClassNode)getRefNode()).setAttributes(fullText);
    	    	} else {
    	    		((ClassNode)getRefNode()).setOperations(fullText);
    	    	}    	    	
    	    }
    	});
    	
    	MenuItem cmItemDelete = new MenuItem("Delete");
    	cmItemDelete.setUserData(tf);
    	cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
    	    public void handle(ActionEvent e) {
    	    	IdentifiedTextField modifiedTextField = (IdentifiedTextField) ((MenuItem) e.getSource()).getUserData();
    	    	String fullText = "";
    	    	for (IdentifiedTextField tf: list) {
    	    		if (!tf.equals(modifiedTextField)) {
            	    	fullText = fullText + list.indexOf(tf) + ";" + tf.getXmiId() + "|" + tf.getText() + System.getProperty("line.separator");
    	    		}
    	    	}    	    	
    	    	fullText = fullText + "-1;" + tf.getXmiId() + "|" + tf.getText() + System.getProperty("line.separator");
    	    	if (type.equals("attribute")) {
    	    		((ClassNode)getRefNode()).setAttributes(fullText);
    	    	} else {
    	    		((ClassNode)getRefNode()).setOperations(fullText);
    	    	}    	    	
    	    }
    	});
    	
    	MenuItem cmItemAdd;
    	if (type.equals("attribute")) {
        	cmItemAdd = new MenuItem("Add attribute");
        	cmItemAdd.setOnAction(event -> {
    	    	addAttribute();
            });            
    	} else {
        	cmItemAdd = new MenuItem("Add operation");
        	cmItemAdd.setOnAction(event -> {
    	    	addOperation();
            });            
    	}
        
       	ContextMenu contextMenu = new ContextMenu();
    	contextMenu.getItems().addAll(cmItemDelete,cmItemAdd);
    	tf.setContextMenu(contextMenu);
    }
    
    private void initLooksAttributeOperation(IdentifiedTextField textField) {
		textField.setFont(Font.font("Verdana", 10));
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        Background background =  new Background(backgroundFill);
        textField.setPadding(new Insets(0));
        textField.setBackground(background);    	
    }
    
    
    public void addAttribute() {
    	IdentifiedTextField textField = new IdentifiedTextField("");
		initLooksAttributeOperation(textField);
		vbox.getChildren().add(2+attributes.size(),textField);    	
    	
    	// Create Handles
    	createHandlesAttributesOperations(textField, attributes, "attribute");
        
        textField.setPromptText("-nome_do_atributo:Tipo");
    	attributes.add(textField);
    }
    
    public void addOperation() {
    	IdentifiedTextField textField = new IdentifiedTextField("");
		initLooksAttributeOperation(textField);
		vbox.getChildren().add(2+attributes.size()+1+operations.size(),textField);    	
		
    	// Create Handles
    	createHandlesAttributesOperations(textField, operations, "operation");
        
        textField.setPromptText("+nome_da_operação()");
        operations.add(textField);
    }    

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    	
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
        	String newValue = (String) evt.getNewValue();
        	// Update text if it was altered
        	if (!title.getText().equals(newValue)) {
        		title.setText(newValue);
        	}            
            if (title.getText() == null || title.getText().equals("")) {
                firstLine.setVisible(false);
            } else {
            	firstLine.setVisible(true);
            }
        } else if (evt.getPropertyName().equals(Constants.changeClassNodeAttributes)) {
        	String newValue = (String) evt.getNewValue();
        	for(String text : newValue.split("\\r?\\n")){
            	String array[] = text.split(";");
            	int ind = Integer.parseInt(array[0]);
            	IdentifiedTextField remoteTextField = new IdentifiedTextField(array[1]);
            	boolean found = false;
            	for (IdentifiedTextField localTextField: attributes) {
            		if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
            			found = true;
            			if (ind != -1) {
                			// Update text if it was altered
                			if (!localTextField.getText().equals(remoteTextField.getText())) {
                    			localTextField.setText(remoteTextField.getText());
                			}
                			// If moved upper or down
                			if (attributes.indexOf(localTextField) != ind) {
                				attributes.remove(localTextField);
                				attributes.add(ind,localTextField);
                			}
            			} else {
                    		attributes.remove(localTextField);
                            vbox.getChildren().remove(localTextField);
            			}
            			break;
            		}
            	}
            	// New attribute
            	if (!found) {
            		attributes.add(ind,remoteTextField);
            		vbox.getChildren().add(2+ind,remoteTextField);
            		initLooksAttributeOperation(remoteTextField);
            	}
        	}
        } else if (evt.getPropertyName().equals(Constants.changeClassNodeOperations)) {
        	String newValue = (String) evt.getNewValue();
        	for(String text : newValue.split("\\r?\\n")){
            	String array[] = text.split(";");
            	int ind = Integer.parseInt(array[0]);
            	IdentifiedTextField remoteTextField = new IdentifiedTextField(array[1]);
            	boolean found = false;
            	for (IdentifiedTextField localTextField: operations) {
            		if (localTextField.getXmiId().equals(remoteTextField.getXmiId())) {
            			found = true;
            			if (ind != -1) {
                			// Update text if it was altered
                			if (!localTextField.getText().equals(remoteTextField.getText())) {
                    			localTextField.setText(remoteTextField.getText());
                			}
                			// If moved upper or down
                			if (operations.indexOf(localTextField) != ind) {
                				operations.remove(localTextField);
                				operations.add(ind,localTextField);
                			}
            			} else {
            				operations.remove(localTextField);
                            vbox.getChildren().remove(localTextField);
            			}
            			break;
            		}
            	}
            	// New operation
            	if (!found) {
            		operations.add(ind,remoteTextField);
            		vbox.getChildren().add(2+attributes.size()+1+ind,remoteTextField);
            		initLooksAttributeOperation(remoteTextField);
            	}
        	}
        }
    }
}
