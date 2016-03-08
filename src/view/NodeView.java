package view;

import javafx.scene.paint.Paint;

/**
 * Created by marcusisaksson on 2016-02-17.
 */
public interface NodeView
{
    public void setFill(Paint p);

    //TODO Shouldn't be used, as this should only get changes from model.
    public void setX(double x);

    //TODO Shouldn't be used, as this should only get changes from model.
    public void setY(double y);

    public void setWidth(double width);

    public void setHeight(double height);

    public double getX();

    public double getY();

    public double getWidth();

    public double getHeight();
}
