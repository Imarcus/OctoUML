package model;

import javafx.geometry.Rectangle2D;

/**
 * Created by chris on 2016-02-15.
 */
public interface Node extends GraphElement {
    public double getX();

    public double getY();

    public double getWidth();

    public double getHeight();

    public Rectangle2D getBounds();

    public String getTitle();

    public void setTitle(String title);
}
