package view;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import model.AbstractNode;
import model.Node;
import model.PackageNode;

/**
 * Created by marcusisaksson on 2016-02-17.
 */
public class PackageNodeView extends AbstractNodeView {

    private PackageNode refNode;
    private Text title;
    private VBox container;
    private StackPane topStackPane;
    private Rectangle top;
    private Rectangle body;
    private final double TOP_HEIGHT_RATIO = 0.2;
    private final double TOP_WIDTH_RATIO = 0.66;
    private final double TOP_MAX_HEIGHT = 50;
    private final double TOP_MAX_WIDTH = 200;

    public PackageNodeView(PackageNode node) {
        super(node);
        refNode = node;
        setChangeListeners();
        title = new Text(node.getTitle());
        //TODO Ugly solution, hardcoded value.
        title.setWrappingWidth(node.getWidth() - 7);
        container = new VBox();
        topStackPane = new StackPane();

        container.setSpacing(0);

        createRectangles();


        topStackPane.getChildren().addAll(top, title);
        container.getChildren().add(topStackPane);
        container.getChildren().add(body);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(top, Pos.CENTER_LEFT);
        setTitleSize();
        //TODO Hardcoded values.
        //stackPane.setMargin(title, new Insets(7, 7, 7, 7));
        this.getChildren().add(container);
        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());

        topStackPane.setStyle(
                "-fx-border-style: solid inside;" +
                        "-fx-border-width: 2;"  +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: yellow;");

    }

    private void setTitleSize(){
        title.resize(top.getHeight(), top.getWidth());
    }

    private void createRectangles(){
        top = new Rectangle();
        body = new Rectangle();
        changeHeight(getRefNode().getHeight());
        changeWidth(getRefNode().getWidth());

        top.setX(getRefNode().getX());
        top.setY(getRefNode().getY());
        top.setFill(Color.LIGHTSKYBLUE);
        top.setStroke(Color.BLACK);

        body.setX(getRefNode().getX());
        body.setY(getRefNode().getY() + top.getHeight());
        body.setFill(Color.LIGHTSKYBLUE);
        body.setStroke(Color.BLACK);

    }

    @Override
    public boolean contains(double x, double y) {
        //If there is a childNode inside this PackageNode, we should return false.
        if (refNode.findNode(new Point2D(x, y)) != null) {
            return false;
        }
        return super.contains(x, y);
    }

    public void setStrokeWidth(double scale) {
        top.setStrokeWidth(scale);
        body.setStrokeWidth(scale);
    }

    public void setFill(Paint p) {
        top.setFill(p);
        body.setFill(p);
    }

    public void setStroke(Paint p) {
        top.setStroke(p);
        body.setStroke(p);
    }

    public Bounds getBounds(){
        return body.getBoundsInParent();
    }

    private void changeHeight(double height){
        setHeight(height);
        top.setHeight(Math.min(TOP_MAX_HEIGHT, height*TOP_HEIGHT_RATIO));
        body.setHeight(height - top.getHeight());
    }

    private void changeWidth(double width){
        setWidth(width);
        top.setWidth(Math.min(TOP_MAX_WIDTH, width*TOP_WIDTH_RATIO));
        body.setWidth(width);
        title.setWrappingWidth(width - 7);

    }

    private void setChangeListeners() {
        getRefNode().titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //TODO Check how long the new string is, and handle that!
                title.setText(newValue);
            }
        });

        getRefNode().xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setX(newValue.doubleValue());
            }
        });

        getRefNode().yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setY(newValue.doubleValue());
            }
        });

        getRefNode().heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeHeight(newValue.doubleValue());
                //setTitleSize();
            }
        });

        getRefNode().widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeWidth(newValue.doubleValue());
                //setTitleSize();
            }
        });
    }
}