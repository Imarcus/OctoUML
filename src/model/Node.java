package model;

import javafx.geometry.Rectangle2D;

/**
 * Interface used by all Node-classes. Represents a UML-class.
 */
public interface Node extends GraphElement {
    double getX();

    double getY();

    double getWidth();

    double getHeight();

    Rectangle2D getBounds();

    String getTitle();

    void setTitle(String title);
}
