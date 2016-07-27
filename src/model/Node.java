package model;

import javafx.geometry.Rectangle2D;

/**
 * Created by chris on 2016-02-15.
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
