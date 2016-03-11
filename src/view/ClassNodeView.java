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
        //attributesRectangle = new Rectangle();
        //operationsRectangle = new Rectangle();
        changeHeight(node.getHeight());
        changeWidth(node.getWidth());
        rectangle.setX(node.getX());
        rectangle.setY(node.getY());

        //attributesRectangle.setX(node.getX());
        //attributesRectangle.setY(node.getY() + titleRectangle.getHeight());

        //operationsRectangle.setX(node.getX());
        //operationsRectangle.setY(node.getY() + titleRectangle.getHeight() + attributesRectangle.getHeight());
    }

    private void changeHeight(double height){
        setHeight(height);
        rectangle.setHeight(height);//Math.min(TOP_MAX_HEIGHT, height*TOP_HEIGHT_RATIO));
        //attributesRectangle.setHeight((height - titleRectangle.getHeight())/2);
        //operationsRectangle.setHeight((height - titleRectangle.getHeight())/2);
    }

    private void changeWidth(double width){
        setWidth(width);
        rectangle.setWidth(width);
        container.setMaxWidth(width);
    }

    private void initVBox(){
        ClassNode node = (ClassNode) getRefNode();


        title = new Text();
        if(node.getTitle() == null) {
            title.setText("Untitled");
        } else {
            title.setText(node.getTitle());
        }
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrappingWidth(node.getWidth() - 7);         //TODO Ugly solution, hardcoded value.
        attributes = new Text(node.getAttributes());
        operations = new Text(node.getOperations());

        firstLine = new Separator();
        firstLine.setMinWidth(node.getWidth());
        firstLine.setMaxWidth(node.getWidth());
        firstLine.setPrefWidth(node.getWidth());
        firstLine.bord

        secondLine = new Separator();
        secondLine.setMinWidth(node.getWidth());
        secondLine.setMaxWidth(node.getWidth());
        secondLine.setPrefWidth(node.getWidth());

        vbox.getChildren().addAll(title, firstLine);
    }

    private void initLooks(){
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);

        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(5,0,0,0));
        StackPane.setAlignment(attributes, Pos.TOP_LEFT);
        StackPane.setMargin(attributes, new Insets(5,0,0,5));
        StackPane.setAlignment(operations, Pos.TOP_LEFT);
        StackPane.setMargin(operations, new Insets(5,0,0,5));
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
                //TODO Check how long the new string is, and handle that!
                title.setText(newValue);
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
            }
        });
    }
}
