package controller;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import model.AbstractEdge;
import model.AbstractNode;
import model.Edge;
import model.GraphElement;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class GraphController {

    //For touch-moving the pane
    private double initMoveX, initMoveY;
    private HashMap<GraphElement, Double>  xInitTranslateMap = new HashMap<>();
    private HashMap<GraphElement, Double>  yInitTranslateMap = new HashMap<>();
    private HashMap<Line, Double> xInitTranslateMapGrid = new HashMap<>();
    private HashMap<Line, Double> yInitTranslateMapGrid = new HashMap<>();
    private HashMap<AnchorPane, Double> xDialogInitTranslateMap = new HashMap<>();
    private HashMap<AnchorPane, Double> yDialogInitTranslateMap = new HashMap<>();

    private double initPaneTranslateX;
    private double initPaneTranslateY;


    private Pane aDrawPane;
    private MainController aMainController;

    //For calculating zooming pivot point
    private double drawPaneXOffset;
    private double drawPaneYOffset;

    public GraphController(Pane pDrawPane, MainController pMainController)
    {
        aDrawPane = pDrawPane;
        aMainController = pMainController;
        drawPaneXOffset = 0;
        drawPaneYOffset = 0;

    }

    public void movePaneStart(List<GraphElement> elements, MouseEvent event)
    {

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

        initPaneTranslateX = aDrawPane.getTranslateX();
        initPaneTranslateY = aDrawPane.getTranslateY();

        for(GraphElement gElement : elements){
            xInitTranslateMap.put(gElement, gElement.getTranslateX());
            yInitTranslateMap.put(gElement, gElement.getTranslateY());
        }

        for(Line line : aMainController.getGrid()){
            xInitTranslateMapGrid.put(line, line.getTranslateX());
            yInitTranslateMapGrid.put(line, line.getTranslateY());
        }

        for (AnchorPane dialog : aMainController.getAllDialogs()) {
            xDialogInitTranslateMap.put(dialog, dialog.getTranslateX());
            yDialogInitTranslateMap.put(dialog, dialog.getTranslateY());
        }
    }

    public void movePane(List<GraphElement> elements, MouseEvent event)
    {
        double offsetX = 0;
        double offsetY = 0;
        if(event.getSource() instanceof javafx.scene.Node){
            offsetX = (event.getSceneX() - initMoveX) * 100/aMainController.getZoomScale();
            offsetY = (event.getSceneY() - initMoveY) * 100/aMainController.getZoomScale();
            drawPaneXOffset += (initMoveX - event.getSceneX());
            drawPaneYOffset += (initMoveY - event.getSceneY());
        } else {
            offsetX = (event.getX() - initMoveX) * 100/aMainController.getZoomScale();
            offsetY = (event.getY() - initMoveY) * 100/aMainController.getZoomScale();
            drawPaneXOffset += (initMoveX - event.getX());
            drawPaneYOffset += (initMoveY - event.getY());
        }

        //TODO Limit graph panningg

        //Drag all nodes
        for (GraphElement gElement : elements)
        {
            //Limit to moving the pane
            //If we are inside the limit OR we are moving away from the limit we allow moving
            /*if(Math.abs(drawPaneXOffset + offsetX) < aDrawPane.getWidth()/2 ||
                    (drawPaneXOffset > 0 && offsetX > 0) ||
                    (drawPaneXOffset < 0 && offsetX < 0)){*/
                gElement.setTranslateX(xInitTranslateMap.get(gElement) + offsetX);
           //}
            /*if(Math.abs(drawPaneYOffset+ offsetY) < aDrawPane.getHeight()/2 ||
                    (drawPaneYOffset > 0 && offsetY > 0) ||
                    (drawPaneYOffset < 0 && offsetY < 0)){*/
                gElement.setTranslateY(yInitTranslateMap.get(gElement) + offsetY);
            //}
        }

        for(Line line : aMainController.getGrid()){
            /*if(Math.abs(drawPaneXOffset + offsetX) < aDrawPane.getWidth()/2 ||
                    (drawPaneXOffset > 0 && offsetX > 0) ||
                    (drawPaneXOffset < 0 && offsetX < 0)) {*/
                line.setTranslateX(xInitTranslateMapGrid.get(line) + offsetX);
            //}
            /*if(Math.abs(drawPaneYOffset + offsetY) < aDrawPane.getHeight()/2 ||
                    (drawPaneYOffset > 0 && offsetY > 0) ||
                    (drawPaneYOffset < 0 && offsetY < 0)){*/
                line.setTranslateY(yInitTranslateMapGrid.get(line) + offsetY);
            //}
        }

        for (AnchorPane dialog : aMainController.getAllDialogs()) {
            dialog.setTranslateX(xDialogInitTranslateMap.get(dialog) + offsetX);
            dialog.setTranslateY(yDialogInitTranslateMap.get(dialog) + offsetY);
        }
    }

    public void movePaneFinished(MouseEvent event)
    {
        xInitTranslateMap.clear();
        yInitTranslateMap.clear();
        initPaneTranslateX = 0;
        initPaneTranslateY = 0;
        initMoveX = 0;
        initMoveY = 0;
    }
    public void zoomPaneStart()
    {
        //Not used
    }

    public void zoomPane(double oldZoom, double newZoom)
    {
        double scale = newZoom/100;
        aDrawPane.setScaleX(scale);
        aDrawPane.setScaleY(scale);
    }

    public void zoomPaneFinished()
    {
        //Not used
    }

    public void resetDrawPaneOffset(){
        drawPaneYOffset = 0;
        drawPaneYOffset = 0;
    }
}
