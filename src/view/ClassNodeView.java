package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import model.AbstractNode;
import model.ClassNode;

/**
 * Created by chris on 2016-02-16.
 */
public class ClassNodeView extends AbstractNodeView implements NodeView {

    private Text title;
    private StackPane stackPane;
    private Rectangle rectangle;

    private final int STROKE_WIDTH = 1;

    public ClassNodeView(ClassNode node) {
        super(node);
        setChangeListeners();
        title = new Text(node.getTitle());
        title.setTextAlignment(TextAlignment.CENTER);
        //TODO Ugly solution, hardcoded value.
        title.setWrappingWidth(node.getWidth() - 7);
        stackPane = new StackPane();
        rectangle = new Rectangle(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        stackPane.getChildren().addAll(rectangle, title);
        stackPane.setAlignment(title, Pos.TOP_CENTER);
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setFill(Color.LIGHTSKYBLUE);
        rectangle.setStroke(Color.BLACK);
        //TODO Hardcoded values.
        //stackPane.setMargin(title, new Insets(7, 7, 7, 7));
        this.getChildren().add(stackPane);

        this.setTranslateX(node.getTranslateX());
        this.setTranslateY(node.getTranslateY());
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
        return rectangle.getBoundsInParent();
    }

    //TODO Maybe needs some Nullchecks etc?
    private void setChangeListeners() {
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
                setHeight(newValue.doubleValue());
                rectangle.setHeight(newValue.doubleValue());
            }
        });

        getRefNode().widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setWidth(newValue.doubleValue());
                rectangle.setWidth(newValue.doubleValue());
                //TODO Ugly
                title.setWrappingWidth(getWidth() - 7);
            }
        });

        getRefNode().titleProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //TODO Check how long the new string is, and handle that!
                title.setText(newValue);
            }
        });
    }
}
