package view.nodes;

import javafx.scene.paint.Paint;

/**
 * Interface used by all NodeView-classes.
 * Visual represenation of Node-class.
 */
public interface NodeView
{
    void setFill(Paint p);

    void setX(double x);

    void setY(double y);

    void setWidth(double width);

    void setHeight(double height);

    double getX();

    double getY();

    double getWidth();

    double getHeight();

    void setSelected(boolean selected);
}
