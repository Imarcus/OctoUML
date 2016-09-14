package model.nodes;

import javafx.geometry.Rectangle2D;
import model.GraphElement;

/**
 * Interface used by all Node-classes. Represents a UML-class.
 */
public interface Node extends GraphElement {

    String getId();

    double getX();

    double getY();

    double getWidth();

    double getHeight();

    void setWidth(double width);

    void setHeight(double height);

    Rectangle2D getBounds();

    String getTitle();

    void setTitle(String title);

    String getType();
}
