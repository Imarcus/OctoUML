package model;

import edu.tamu.core.sketch.Point;
import edu.tamu.core.sketch.Stroke;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import util.Constants;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * The model-representation of a Sketch.
 * TODO This now holds it's own View (Path). Should maybe be refactored to a "SketchView"-class.
 */
public class Sketch implements GraphElement, Serializable {
    private Path path;
    private GraphElement recognizedElement;
    private Stroke stroke;
    private boolean selected = false;

    private Color color = Color.BLACK;

    private static final long serialVersionUID = 1L;
    private static int objectCount = 0; //Used to ID instance
    private int id = 0;

    public transient PropertyChangeSupport changes = new PropertyChangeSupport(this);


    public Sketch(){
        //TODO, should this make a copy of the path?
        path = new Path();
        stroke = new Stroke();
        path.toFront();
        path.setStrokeWidth(2);
        path.setStroke(Color.BLACK);
        id = ++objectCount;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            path.setStroke(Constants.selected_sketch_color);
        } else {
            path.setStroke(color);
        }
    }

    public void setStart(double x, double y){
        path.getElements()
                .add(new MoveTo(x, y));
        changes.firePropertyChange(Constants.changeSketchStart, null, new Point2D(x,y));
    }

    public void addPoint(double x, double y) {
        path.getElements()
                .add(new LineTo(x, y));
        stroke.addPoint(new Point(x, y));
        changes.firePropertyChange(Constants.changeSketchPoint, null, new Point2D(x,y));
    }

    public void setStartRemote(double x, double y){
        path.getElements()
                .add(new MoveTo(x, y));
    }

    public void addPointRemote(double x, double y) {
        path.getElements()
                .add(new LineTo(x, y));
        stroke.addPoint(new Point(x, y));
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        changes.firePropertyChange(Constants.changeSketchStroke, null, stroke);
    }

    public void setStrokeRemote(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the recognized element for this sketch.
     * @param element
     */
    public void setRecognizedElement(GraphElement element) {
        this.recognizedElement = element;
    }

    /**
     * Get the GraphElement that this sketch was recognized as.
     * @return a GraphElement if present, otherwise null
     */
    public GraphElement getRecognizedElement() {
        return recognizedElement;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void setTranslateX(double x) {
        path.setTranslateX(x);
        changes.firePropertyChange(Constants.changeSketchTranslateX, null, x);
    }

    @Override
    public void setTranslateY(double y) {
        path.setTranslateY(y);
        changes.firePropertyChange(Constants.changeSketchTranslateY, null, y);
    }

    @Override
    public void setScaleX(double x) {
        path.setScaleX(x);
    }

    @Override
    public void setScaleY(double y) {
        path.setScaleY(y);
    }

    public void remoteSetTranslateX(double x) {
        path.setTranslateX(x);
    }

    public void remoteSetTranslateY(double y) {
        path.setTranslateY(y);
    }

    public double getTranslateX() {
        return path.getTranslateX();
    }

    @Override
    public double getTranslateY() {
        return path.getTranslateY();
    }

    @Override
    public double getScaleX() {
        return path.getScaleX();
    }

    @Override
    public double getScaleY() {
        return path.getScaleY();
    }

    public String getId(){
        return "SKETCH_" + id;
    }

    public static void incrementObjectCount(){
        objectCount++;
    }

    public void addPropertyChangeListener(PropertyChangeListener l){
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangelistener(PropertyChangeListener l){
        changes.removePropertyChangeListener(l);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color pColor) {
        this.color = pColor;
        path.setStroke(color);
    }

}
