package model;

/**
 * Used for Recognition.
 */
public interface GraphElement {
    public void setTranslateX(double x);
    public void setTranslateY(double y);
    public void setScaleX(double x);
    public void setScaleY(double y);
    public double getTranslateX();
    public double getTranslateY();
    public double getScaleX();
    public double getScaleY();
}
