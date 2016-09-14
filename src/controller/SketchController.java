package controller;

import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.*;
import view.nodes.AbstractNodeView;

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
    private AbstractDiagramController mController;

    private HashMap<Integer, Sketch> currentSketches = new HashMap<>();
    private Sketch currentSketch;

    private double initMoveX, initMoveY;
    private HashMap<Sketch, Point2D.Double> initTranslateMap = new HashMap<>();
    private ArrayList<Sketch> toBeMoved = new ArrayList<>();

    public Color color = Color.BLACK;

    public SketchController(Pane pDrawPane, AbstractDiagramController diagramController) {
        this.aDrawPane = pDrawPane;
        this.mController = diagramController;

    }
    public void onTouchPressed(InputEvent event) {
        Sketch sketch = new Sketch();
        mController.addSketch(sketch, false, false);

        double x, y;
        if(event instanceof TouchEvent){
            x = ((TouchEvent) event).getTouchPoint().getX();
            y = ((TouchEvent) event).getTouchPoint().getY();
        } else { //event instanceof mouseevent
            x = ((MouseEvent)event).getX();
            y = ((MouseEvent)event).getY();
        }

        double xPoint;
        double yPoint;
        if(event.getSource() instanceof AbstractNodeView){
            xPoint = ((AbstractNodeView)event.getSource()).getTranslateX() + x;
            yPoint = ((AbstractNodeView)event.getSource()).getTranslateY() + y;
        } else {
            xPoint = x;
            yPoint = y;
        }
        sketch.setColor(color);
        sketch.setStart(xPoint, yPoint);

        if(event instanceof TouchEvent) {
            currentSketches.put(((TouchEvent)event).getTouchPoint().getId(), sketch);
        } else { //event instanceof mouseevent
            currentSketch = sketch;
        }
    }

    public void onTouchMoved(InputEvent event) {
        Sketch sketch;
        double xPoint;
        double yPoint;

        if(event instanceof TouchEvent){
            sketch = currentSketches.get(((TouchEvent)event).getTouchPoint().getId());

            if(event.getSource() instanceof AbstractNodeView){
                xPoint = ((AbstractNodeView)event.getSource()).getTranslateX() + ((TouchEvent)event).getTouchPoint().getX();
                yPoint = ((AbstractNodeView)event.getSource()).getTranslateY() + ((TouchEvent)event).getTouchPoint().getY();
            } else {
                xPoint = ((TouchEvent)event).getTouchPoint().getX();
                yPoint = ((TouchEvent)event).getTouchPoint().getY();
            }
        } else { //event instanceof mouseevent
            sketch = currentSketch;

            if(event.getSource() instanceof AbstractNodeView){
                xPoint = ((AbstractNodeView)event.getSource()).getTranslateX() + ((MouseEvent)event).getX();
                yPoint = ((AbstractNodeView)event.getSource()).getTranslateY() + ((MouseEvent)event).getY();
            } else {
                xPoint = ((MouseEvent)event).getX();
                yPoint = ((MouseEvent)event).getY();
            }
        }

        sketch.addPoint(xPoint, yPoint);
    }

    public void onTouchReleased(InputEvent event) {
        if(event instanceof TouchEvent) {
            currentSketches.remove(((TouchEvent) event).getTouchPoint().getId());
        }
    }

    public boolean currentlyDrawing() {
        return !currentSketches.isEmpty();
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
