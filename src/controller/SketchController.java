package controller;

import edu.tamu.core.sketch.Point;
import edu.tamu.core.sketch.Shape;
import edu.tamu.core.sketch.Stroke;
import edu.tamu.recognition.paleo.PaleoConfig;
import edu.tamu.recognition.paleo.PaleoSketchRecognizer;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import model.*;
import view.AbstractNodeView;

import java.awt.geom.Point2D;
import java.util.ArrayList;
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

    private double initMoveX, initMoveY;
    private HashMap<Sketch, Point2D.Double> initTranslateMap = new HashMap<>();
    private ArrayList<Sketch> toBeMoved = new ArrayList<>();

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

        double xPoint;
        double yPoint;
        if(event.getSource() instanceof AbstractNodeView){
            xPoint = ((AbstractNodeView)event.getSource()).getTranslateX() + event.getTouchPoint().getX();
            yPoint = ((AbstractNodeView)event.getSource()).getTranslateY() + event.getTouchPoint().getY();
        } else {
            xPoint = event.getTouchPoint().getX();
            yPoint = event.getTouchPoint().getY();
        }
        currentSketch.getPath().getElements()
                .add(new MoveTo(xPoint, yPoint));

        aDrawPane.getChildren().add(currentSketch.getPath());
        currentSketches.put(event.getTouchPoint().getId(), currentSketch);
        currentStrokes.put(event.getTouchPoint().getId(), currentStroke);

    }

    public void onTouchMoved(TouchEvent event) {
        Sketch currentSketch = currentSketches.get(event.getTouchPoint().getId());
        Stroke currentStroke = currentStrokes.get(event.getTouchPoint().getId());

        double xPoint;
        double yPoint;
        if(event.getSource() instanceof AbstractNodeView){
            xPoint = ((AbstractNodeView)event.getSource()).getTranslateX() + event.getTouchPoint().getX();
            yPoint = ((AbstractNodeView)event.getSource()).getTranslateY() + event.getTouchPoint().getY();
        } else {
            xPoint = event.getTouchPoint().getX();
            yPoint = event.getTouchPoint().getY();
        }
        currentSketch.getPath().getElements()
                .add(new LineTo(xPoint, yPoint));
        currentStroke.addPoint(new Point(xPoint, yPoint));
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

    public void moveSketchStart(MouseEvent event) {
        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();
        Point2D.Double initTranslate;
        for (Sketch sketch : mController.getSelectedSketches()) {
            initTranslate = new Point2D.Double(sketch.getTranslateX(), sketch.getTranslateY());
            initTranslateMap.put(sketch, initTranslate);
            toBeMoved.add(sketch);
        }
    }

    public void moveSketches(MouseEvent event) {
        double offsetX = event.getSceneX() - initMoveX;
        double offsetY = event.getSceneY() - initMoveY;

        //Drag all selected sketches.
        for(Sketch sketch : toBeMoved)
        {
            sketch.setTranslateX(initTranslateMap.get(sketch).getX() + offsetX);
            sketch.setTranslateY(initTranslateMap.get(sketch).getY() + offsetY);
        }
    }

    public double[] moveSketchFinished(MouseEvent event) {
        toBeMoved.clear();
        initTranslateMap.clear();
        double[] deltaTranslateVector = new double[2];
        deltaTranslateVector[0] = event.getSceneX() - initMoveX;
        deltaTranslateVector[1] = event.getSceneY() - initMoveY;
        return deltaTranslateVector;
    }
}
