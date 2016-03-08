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

    public GraphController(Pane pDrawPane, MainController pMainController)
    {
        aDrawPane = pDrawPane;
        aMainController = pMainController;
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
        double offsetX = event.getSceneX() - initMoveX;
        double offsetY = event.getSceneY() - initMoveY;

        //Drag all nodes
        int i = 0;
        for (GraphElement gElement : elements)
        {
            gElement.setTranslateX(xInitTranslateList.get(i) + offsetX);
            gElement.setTranslateY(yInitTranslateList.get(i) + offsetY);
            i++;
        }
    }

    public void movePaneFinished()
    {
        xInitTranslateList.clear();
        yInitTranslateList.clear();
    }
    public void zoomPaneStart()
    {
        //Not used
    }

    public void zoomPane(double oldZoom, double newZoom)
    {


        for (AbstractNode n : aMainController.getGraphModel().getAllNodes()) {
            double scale = n.getScaleX(); // currently we only use Y, same value is used for X
            double oldScale = scale;


            scale = newZoom;//*= zoomFactor;
            System.out.println("scale: " + scale);

            double f = (scale / oldScale) - 1;

            double dx = ((aDrawPane.getWidth()/2) - (n.getWidth() / 2 + n.getX())) * scale;
            double dy = ((aDrawPane.getHeight()/2) - (n.getHeight() / 2 + n.getY())) * scale;

            n.setScaleX(scale);
            n.setScaleY(scale);

            // note: pivot value must be untransformed, i. e. without scaling
            n.setTranslateX(n.getTranslateX() - (f * dx));
            n.setTranslateY(n.getTranslateY() - (f * dy));
        }
        for(Edge e : aMainController.getGraphModel().getAllEdges()){
            ((AbstractEdge) e).setZoom(newZoom);
        }
    }

    public void zoomPaneFinished()
    {
        //Not used
    }
}
