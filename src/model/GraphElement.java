package model;

/**
 * Used for Recognition.
 */
public interface GraphElement {
    void setTranslateX(double x);
    void setTranslateY(double y);
    void setScaleX(double x);
    void setScaleY(double y);
    double getTranslateX();
    double getTranslateY();
    double getScaleY();
    double getScaleX();
    String getId();
}
