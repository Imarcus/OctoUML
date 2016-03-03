package model;

import edu.tamu.core.sketch.Stroke;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;

/**
 * The model-representation of a Sketch.
 * TODO This now holds it's own View (Path). Should maybe be refactored to a "SketchView"-class.
 */
public class Sketch implements GraphElement{
    private Path path;
    private GraphElement recognizedElement;
    private Stroke stroke;
    private boolean selected = false;

    /**
     * Creates this Sketch
     * @param path
     */
    public Sketch(Path path){
        //TODO, should this make a copy of the path?
        this.path = path;
        path.toFront();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            path.setStroke(Color.RED);
        } else {
            path.setStroke(Color.BLACK);
        }
    }

    public void setStroke(Stroke stroke) {
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
    }

    @Override
    public void setTranslateY(double y) {
        path.setTranslateY(y);
    }

    @Override
    public void setScaleX(double x) {
        path.setScaleX(x);
    }

    @Override
    public void setScaleY(double y) {
        path.setScaleY(y);
    }

    @Override
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
}
