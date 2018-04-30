package view.nodes;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
import javafx.scene.control.TextField;
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
    private List<TextField> attributes;
    private List<TextField> operations;

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
    	Iterator i = attributes.iterator();
    	while (i.hasNext()){
    		IdentifiedTextField textField = (IdentifiedTextField) i.next();
        	textField.textProperty().addListener(new ChangeListener<String>() {
        	    @Override
        	    public void changed(ObservableValue<? extends String> observable,
        	            String oldValue, String newValue) {
        	    	String fullText = attributes.indexOf(textField) + ";" + textField.getXmiId() + "|" +  newValue;
        	    	((ClassNode)getRefNode()).setAttributes(fullText);
        	        System.out.println("Attribute changed to " + fullText + ")\n");
        	    }
        	});
        }
    	i = operations.iterator();
    	while (i.hasNext()){
    		IdentifiedTextField textField = (IdentifiedTextField) i.next();
        	textField.textProperty().addListener(new ChangeListener<String>() {
        	    @Override
        	    public void changed(ObservableValue<? extends String> observable,
        	            String oldValue, String newValue) {
        	    	String fullText = operations.indexOf(textField) + ";" + textField.getXmiId() + "|" +  newValue;
        	    	((ClassNode)getRefNode()).setOperations(fullText);
        	        System.out.println("Operation changed to " + fullText + ")\n");
        	    }
        	});
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
            	TextField textfield = new IdentifiedTextField(text);
            	textfield.setFont(Font.font("Verdana", 10));
            	attributes.add(textfield);
            }
        }
        operations = new ArrayList<>();
        if (node.getOperations() != null) {
            for(String text : node.getOperations().split("\\r?\\n")){
            	TextField textfield = new IdentifiedTextField(text);
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
    	TextField textfield;
    	Iterator i;
    	Background background;
    	
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);
        StackPane.setAlignment(title, Pos.CENTER);
        background =  new Background(new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY));
        title.setBackground(background);
        i = attributes.iterator();
        while (i.hasNext()) {
        	textfield = (TextField) i.next();
        	textfield.setPadding(new Insets(0));
        	textfield.setBackground(background);
        }
        i = operations.iterator();
        while (i.hasNext()) {
        	textfield = (TextField) i.next();
        	textfield.setPadding(new Insets(0));
        	textfield.setBackground(background);        	
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


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	Iterator i;
    	IdentifiedTextField textField;
    	String newValue, array[];
    	int ind;
    	
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
            title.setText((String) evt.getNewValue());
            if (title.getText() == null || title.getText().equals("")) {
                firstLine.setVisible(false);
            } else {
                firstLine.setVisible(true);
            }
        } else if (evt.getPropertyName().equals(Constants.changeClassNodeAttributes)) {
        	newValue = (String) evt.getNewValue();
        	array = newValue.split(";");
        	ind = Integer.parseInt(array[0]);
        	textField = new IdentifiedTextField(array[1]);
        	if (attributes.contains(textField)) {
        		attributes.remove(textField);
        	}
        	// ind = -1 means the field was deleted
        	if (ind != -1) {
           		attributes.add(ind,textField);
        	}
        } else if (evt.getPropertyName().equals(Constants.changeClassNodeOperations)) {
        	newValue = (String) evt.getNewValue();
        	array = newValue.split(";");
        	ind = Integer.parseInt(array[0]);
        	textField = new IdentifiedTextField(array[1]);
        	if (operations.contains(textField)) {
        		operations.remove(textField);
        	}
        	// ind = -1 means the field was deleted
        	if (ind != -1) {
        		operations.add(ind,textField);
        	}
            if (operations.isEmpty()) {
                secondLine.setVisible(false);
            } else {
                secondLine.setVisible(true);
            }
        }
    }
}
