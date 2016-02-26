package controller;

import edu.tamu.core.sketch.Point;
import edu.tamu.core.sketch.Shape;
import edu.tamu.core.sketch.Stroke;
import edu.tamu.recognition.paleo.PaleoConfig;
import edu.tamu.recognition.paleo.PaleoSketchRecognizer;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import model.*;

import java.util.HashMap;

/**
 * Created by chris on 2016-02-15.
 */
public class SketchController {
    //private Sketch currentSketch;
    //private Stroke currentStroke;
    private Pane aDrawPane;
    private MainController mController;

    private HashMap<Integer, Sketch> currentSketches = new HashMap<>();
    private HashMap<Integer, Stroke> currentStrokes = new HashMap<>();

    public SketchController(Pane pDrawPane, MainController mainController) {
        this.aDrawPane = pDrawPane;
        this.mController = mainController;

    }
    public void onTouchPressed(TouchEvent event) {
        Path initPath = new Path();
        initPath.toFront();
        Sketch currentSketch = new Sketch(initPath);
        Stroke currentStroke = new Stroke();
        //TODO Hardcoded strokeWidth.
        currentSketch.getPath().setStrokeWidth(2);
        currentSketch.getPath().setStroke(Color.BLACK);
        currentSketch.getPath().getElements()
                .add(new MoveTo(event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY()));
        aDrawPane.getChildren().add(currentSketch.getPath());
        currentSketches.put(event.getTouchPoint().getId(), currentSketch);
        currentStrokes.put(event.getTouchPoint().getId(), currentStroke);

    }

    public void onTouchMoved(TouchEvent event) {
        Sketch currentSketch = currentSketches.get(event.getTouchPoint().getId());
        Stroke currentStroke = currentStrokes.get(event.getTouchPoint().getId());
        currentSketch.getPath().getElements()
                .add(new LineTo(event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY()));
        currentStroke.addPoint(new Point(event.getTouchPoint().getSceneX(), event.getTouchPoint().getSceneY()));
    }

    public Sketch onTouchReleased(TouchEvent event) {
        Sketch currentSketch = currentSketches.get(event.getTouchPoint().getId());
        Stroke currentStroke = currentStrokes.get(event.getTouchPoint().getId());
        currentSketch.setStroke(currentStroke);
        Sketch sketch = currentSketch;

        currentSketches.remove(event.getTouchPoint().getId());
        currentStrokes.remove(event.getTouchPoint().getId());

        return sketch;
    }

    public boolean currentlyDrawing() {
        return !currentStrokes.isEmpty();
    }
}
