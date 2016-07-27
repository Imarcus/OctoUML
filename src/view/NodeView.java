package view;

import javafx.scene.paint.Paint;

/**
 * Created by marcusisaksson on 2016-02-17.
 */
public interface NodeView
{
    void setFill(Paint p);

    //TODO Shouldn't be used, as this should only get changes from model.
    void setX(double x);

    //TODO Shouldn't be used, as this should only get changes from model.
    void setY(double y);

    void setWidth(double width);

    void setHeight(double height);

    double getX();

    double getY();

    double getWidth();

    double getHeight();

    void setSelected(boolean selected);
}
