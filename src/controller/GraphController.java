package controller;

import javafx.scene.input.MouseEvent;
import model.AbstractEdge;
import model.AbstractNode;
import model.Edge;
import model.GraphElement;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class GraphController {

    //For touch-moving the pane
    private double initMoveX, initMoveY;
    private ArrayList<Double> xInitTranslateList = new ArrayList<>();
    private ArrayList<Double> yInitTranslateList = new ArrayList<>();


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

        for(GraphElement gElement : elements){
            xInitTranslateList.add(gElement.getTranslateX());
            yInitTranslateList.add(gElement.getTranslateY());
        }
    }

    public void movePane(List<GraphElement> elements, MouseEvent event)
    {
        double offsetX = 0;
        double offsetY = 0;
        if(event.getSource() instanceof javafx.scene.Node){
            offsetX = (event.getSceneX() - initMoveX) * 100/aMainController.getZoomScale();
            offsetY = (event.getSceneY() - initMoveY) * 100/aMainController.getZoomScale();
        } else {
            offsetX = event.getX() - initMoveX;
            offsetY = event.getY() - initMoveY;
        }

        //Drag all nodes
        int i = 0;
        for (GraphElement gElement : elements)
        {
            gElement.setTranslateX(xInitTranslateList.get(i) + offsetX);
            gElement.setTranslateY(yInitTranslateList.get(i) + offsetY);
            i++;
        }
    }

    public void movePaneFinished(MouseEvent event)
    {
        drawPaneXOffset += (initMoveX - event.getX());
        drawPaneYOffset += (initMoveY - event.getY());

        xInitTranslateList.clear();
        yInitTranslateList.clear();
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
