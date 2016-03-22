package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
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

    private Rectangle rectangle;

    private StackPane container;
    private VBox vbox;

    private Separator firstLine;
    private Separator secondLine;

    private final double TOP_MAX_HEIGHT = 50;
    private final double TOP_HEIGHT_RATIO = 0.2;
    private final int STROKE_WIDTH = 1;

    public ClassNodeView(ClassNode node) {
        super(node);
        setChangeListeners();

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

        vbox.setMaxWidth(width);

        firstLine.setMaxWidth(width);

        secondLine.setMaxWidth(width);
    }

    private void initVBox(){
        ClassNode node = (ClassNode) getRefNode();

        vbox.setPadding(new Insets(5, 0, 5, 0));
        vbox.setSpacing(5);

        StackPane titlePane = new StackPane();

        firstLine = new Separator();
        firstLine.setMaxWidth(node.getWidth());

        secondLine = new Separator();
        secondLine.setMaxWidth(node.getWidth());

        title = new Text();
        title.setFont(Font.font("Verdana", 10));
        if(node.getTitle() != null) {
            title.setText(node.getTitle());
        } else {
            firstLine.setVisible(false);
        }
        title.setTextAlignment(TextAlignment.CENTER);

        attributes = new Text(node.getAttributes());
        attributes.setFont(Font.font("Verdana", 10));

        operations = new Text(node.getOperations());
        operations.setFont(Font.font("Verdana", 10));


        if(operations.getText() == null || operations.getText().equals("")){
            secondLine.setVisible(false);
        }

        titlePane.getChildren().add(title);
        vbox.getChildren().addAll(titlePane, firstLine, attributes, secondLine, operations);
    }

    private void initLooks(){
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);

        title.setWrappingWidth(getRefNode().getWidth() - 7);         //TODO Ugly solution, hardcoded value.
        attributes.setWrappingWidth(getRefNode().getWidth() - 7);
        operations.setWrappingWidth(getRefNode().getWidth() - 7);

        //StackPane.setAlignment(title, Pos.TOP_CENTER);
        //VBox.setMargin(title, new Insets(5,0,0,0));
        //StackPane.setAlignment(attributes, Pos.TOP_LEFT);
        StackPane.setAlignment(title, Pos.CENTER);
        VBox.setMargin(attributes, new Insets(5,0,0,5));
        VBox.setMargin(operations, new Insets(5,0,0,5));
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
                title.setText(newValue);
                if(title.getText() == null || title.getText().equals("")){
                    firstLine.setVisible(false);
                } else {
                    firstLine.setVisible(true);
                }
            }
        });

        refNode.attributesProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                attributes.setText(newValue);
            }
        });

        refNode.operationsProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                operations.setText(newValue);
                if(operations.getText() == null || operations.getText().equals("")){
                    secondLine.setVisible(false);
                } else {
                    secondLine.setVisible(true);
                }
            }
        });
    }
}
