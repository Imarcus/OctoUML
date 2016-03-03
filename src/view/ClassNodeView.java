package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import model.ClassNode;

/**
 * Created by chris on 2016-02-16.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {

    private Text title;
    private Text attributes;
    private Text operations;

    private StackPane titleStackPane;
    private StackPane attributesStackPane;
    private StackPane operationsStackPane;

    private Rectangle titleRectangle;
    private Rectangle attributesRectangle;
    private Rectangle operationsRectangle;

    private VBox container;

    private final double TOP_MAX_HEIGHT = 50;
    private final double TOP_HEIGHT_RATIO = 0.2;
    private final int STROKE_WIDTH = 1;

    public ClassNodeView(ClassNode node) {
        super(node);
        setChangeListeners();

        container = new VBox();

        createTexts();
        createRectangles();


        titleStackPane = new StackPane();
        titleStackPane.getChildren().addAll(titleRectangle, title);

        attributesStackPane = new StackPane();
        attributesStackPane.getChildren().addAll(attributesRectangle, attributes);

        operationsStackPane = new StackPane();
        operationsStackPane.getChildren().addAll(operationsRectangle, operations);

        initLooks();

        container.getChildren().addAll(titleStackPane, attributesStackPane, operationsStackPane);
        this.getChildren().add(container);

        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());
    }

    private void createRectangles(){
        ClassNode node = (ClassNode) getRefNode();
        titleRectangle = new Rectangle();
        attributesRectangle = new Rectangle();
        operationsRectangle = new Rectangle();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        titleRectangle.setX(node.getX());
        titleRectangle.setY(node.getY());

        attributesRectangle.setX(node.getX());
        attributesRectangle.setY(node.getY() + titleRectangle.getHeight());

        operationsRectangle.setX(node.getX());
        operationsRectangle.setY(node.getY() + titleRectangle.getHeight() + attributesRectangle.getHeight());
    }

    private void changeHeight(double height){
        //setHeight(height);
        container.setPrefHeight(height);
        titleRectangle.setHeight(Math.min(TOP_MAX_HEIGHT, height*TOP_HEIGHT_RATIO));
        attributesRectangle.setHeight((height - titleRectangle.getHeight())/2);
        operationsRectangle.setHeight((height - titleRectangle.getHeight())/2);
    }

    private void changeWidth(double width){
        //setWidth(width);
        container.setPrefWidth(width);
        titleRectangle.setWidth(width);
        attributesRectangle.setWidth(width);
        operationsRectangle.setWidth(width);
    }

    private void createTexts(){
        ClassNode node = (ClassNode) getRefNode();
        title = new Text(node.getTitle());
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(node.getWidth() - 7);         //TODO Ugly solution, hardcoded value.

        attributes = new Text();
        operations = new Text();
    }

    private void initLooks(){
        titleRectangle.setStrokeWidth(STROKE_WIDTH);
        titleRectangle.setFill(Color.LIGHTSKYBLUE);
        titleRectangle.setStroke(Color.BLACK);

        attributesRectangle.setStrokeWidth(STROKE_WIDTH);
        attributesRectangle.setFill(Color.LIGHTSKYBLUE);
        attributesRectangle.setStroke(Color.BLACK);

        operationsRectangle.setStrokeWidth(STROKE_WIDTH);
        operationsRectangle.setFill(Color.LIGHTSKYBLUE);
        operationsRectangle.setStroke(Color.BLACK);

        /*StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(5,0,0,0));
        StackPane.setAlignment(attributes, Pos.TOP_LEFT);
        StackPane.setMargin(attributes, new Insets(5,0,0,5));
        StackPane.setAlignment(operations, Pos.TOP_LEFT);
        StackPane.setMargin(operations, new Insets(5,0,0,5));*/
    }

    public void setStrokeWidth(double scale){
        titleRectangle.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
        titleRectangle.setFill(p);
        attributesRectangle.setFill(p);
        operationsRectangle.setFill(p);
    }

    public void setStroke(Paint p) {
        titleRectangle.setStroke(p);
        attributesRectangle.setStroke(p);
        operationsRectangle.setStroke(p);
    }

    public Bounds getBounds(){
        return container.getBoundsInParent();
    }

    //TODO Maybe needs some Nullchecks etc?
    private void setChangeListeners() {
        ClassNode refNode = (ClassNode) getRefNode();
        refNode.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setX(newValue.doubleValue());
            }
        });

        refNode.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setY(newValue.doubleValue());
            }
        });

        refNode.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeHeight(newValue.doubleValue());
            }
        });

        refNode.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeWidth(newValue.doubleValue());
            }
        });

        refNode.titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //TODO Check how long the new string is, and handle that!
                title.setText(newValue);
            }
        });

        refNode.attributesProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                attributes.setText(newValue);
                //updateSize(refNode.getWidth(), refNode.getHeight());
            }
        });

        refNode.operationsProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                operations.setText(newValue);
                //updateSize(refNode.getWidth(), refNode.getHeight());
            }
        });
    }
}
